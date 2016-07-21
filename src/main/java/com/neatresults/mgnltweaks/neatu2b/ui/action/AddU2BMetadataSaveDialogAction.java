/**
 *
 * Copyright 2016 by Jan Haderka <jan.haderka@neatresults.com>
 *
 * This file is part of neat-u2b module.
 *
 * Neat-u2b is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Neat-tweaks is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with neat-u2b.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0 <http://www.gnu.org/licenses/gpl.txt>
 *
 * Should you require distribution under alternative license in order to
 * use neat-u2b commercially, please contact owner at the address above.
 *
 */
package com.neatresults.mgnltweaks.neatu2b.ui.action;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rest.client.registry.RestClientRegistry;
import info.magnolia.resteasy.client.RestEasyClient;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.neatu2b.NeatU2b;
import com.neatresults.mgnltweaks.neatu2b.restclient.U2BService;
import com.neatresults.mgnltweaks.neatu2b.ui.action.AddU2BMetadataSaveDialogAction.Definition;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * SaveDialogAction that will position saved node as first sibling instead of default (last).
 */
public class AddU2BMetadataSaveDialogAction extends SaveDialogAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(AddU2BMetadataSaveDialogAction.class);

    private final RestClientRegistry restClientRegistry;

    private final NeatU2b u2bModule;

    @Inject
    public AddU2BMetadataSaveDialogAction(Definition definition, Item item, EditorValidator validator, EditorCallback callback) {
        super(definition, item, validator, callback);
        this.restClientRegistry = Components.getComponent(RestClientRegistry.class);
        this.u2bModule = Components.getComponent(ModuleRegistry.class).getModuleInstance(NeatU2b.class);
    }

    @Override
    protected void setNodeName(Node node, JcrNodeAdapter item) throws RepositoryException {
        super.setNodeName(node, item);

        // tricky thing. we need to reorder node after validation is checked and before callback is executed ...
        // ... the only place to do so short of reimplementing execute() is here
        String prefix = getDefinition().getIdFieldName();
        RestEasyClient client = null;
        U2BService service = null;
        try {
            client = (RestEasyClient) restClientRegistry.getRestClient("youtube");
            service = client.getClientService(U2BService.class);
        } catch (RegistrationException e) {
            log.error("Failed to get a client for [" + U2BService.class.getName() + "] with: " + e.getMessage(), e);
        }
        if (service != null) {
            String id = getVideoId();
            String key = u2bModule.getGoogleKey();
            JsonNode response = service.meta(id, "snippet", key);
            try {
                if (response.get("items").getElements().hasNext()) {
                    JsonNode videoItem = response.get("items").getElements().next();
                    String description = videoItem.get("snippet").get("description").getTextValue();
                    node.setProperty(prefix + "Description", description);
                    String title = videoItem.get("snippet").get("title").getTextValue();
                    node.setProperty(prefix + "Title", title);
                    Iterator<Entry<String, JsonNode>> thumbs = videoItem.get("snippet").get("thumbnails").getFields();
                    Node thumbsNode;
                    String thumbsNodeName = prefix + "Thumbs";
                    if (node.hasNode(thumbsNodeName)) {
                        thumbsNode = node.getNode(thumbsNodeName);
                    } else {
                        thumbsNode = node.addNode(thumbsNodeName, NodeTypes.ContentNode.NAME);
                    }
                    NodeIterator iter = thumbsNode.getNodes();
                    // get rid of old data
                    while (iter.hasNext()) {
                        iter.nextNode().remove();
                    }
                    while (thumbs.hasNext()) {
                        Entry<String, JsonNode> entry = thumbs.next();
                        Node aThumb = thumbsNode.addNode(entry.getKey(), NodeTypes.ContentNode.NAME);
                        aThumb.setProperty("url", entry.getValue().get("url").getTextValue());
                        aThumb.setProperty("width", entry.getValue().get("width").getLongValue());
                        aThumb.setProperty("height", entry.getValue().get("height").getLongValue());
                    }
                    String publishedAt = videoItem.get("snippet").get("publishedAt").getTextValue();
                    node.setProperty(prefix + "PublishedAt", publishedAt);
                }
            } catch (Exception e) {
                log.error("Failed to parse the video metadata.", e);
            }

            response = service.meta(id, "contentDetails", key);
            try {
                if (response.get("items").getElements().hasNext()) {
                    JsonNode videoItem = response.get("items").getElements().next();
                    String duration = videoItem.get("contentDetails").get("duration").getTextValue();
                    node.setProperty(prefix + "Duration", duration);
                    String definition = videoItem.get("contentDetails").get("definition").getTextValue();
                    node.setProperty(prefix + "Definition", definition);
                }
            } catch (Exception e) {
                log.error("Failed to parse the video duration.", e);
            }

        }
    }

    public String getVideoId() {
        String fieldName = getDefinition().getIdFieldName();
        Property prop = item.getItemProperty(fieldName);
        if (prop == null) {
            throw new NullPointerException(fieldName + " is not set or name of the required field for this dialog is not correctly configured.");
        }
        String maybeId = (String) prop.getValue();
        if (maybeId == null) {
            return null;
        }
        if (maybeId.startsWith("http")) {
            maybeId = StringUtils.substringBefore(StringUtils.substringAfter(maybeId, "?v="), "&");
        }
        return maybeId;
    }

    /**
     * Definition for the class above.
     */
    public static class Definition extends SaveDialogActionDefinition {

        private String idFieldName = "youtube";

        public Definition() {
            setImplementationClass(AddU2BMetadataSaveDialogAction.class);
        }

        public String getIdFieldName() {
            return idFieldName;
        }

        public void setIdFieldName(String idFieldName) {
            this.idFieldName = idFieldName;
        }
    }
}
