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
package com.neatresults.mgnltweaks.neatu2b.ui.form.field.transformer;

import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.predicate.JCRMgnlPropertyHidingPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rest.client.registry.RestClientRegistry;
import info.magnolia.resteasy.client.RestEasyClient;
import info.magnolia.ui.form.field.transformer.composite.CompositeTransformer;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.neatu2b.NeatU2b;
import com.neatresults.mgnltweaks.neatu2b.restclient.U2BService;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * Expands single field with youtube video id/link to set of fields containing also metadata about the video.
 */
public class U2BFieldTransformer extends CompositeTransformer {

    private static final Logger log = LoggerFactory.getLogger(U2BFieldTransformer.class);
    protected List<String> fieldsName;
    private final RestClientRegistry restClientRegistry;

    private final NeatU2b u2bModule;

    @Inject
    public U2BFieldTransformer(Item relatedFormItem, U2BFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName) {
        super(relatedFormItem, definition, type, fieldsName);
        this.restClientRegistry = Components.getComponent(RestClientRegistry.class);
        this.u2bModule = Components.getComponent(ModuleRegistry.class).getModuleInstance(NeatU2b.class);
    }

    @Override
    protected String getCompositePropertyName(String propertyName) {
        if (propertyName.equals("Id")) {
            return propertyPrefix;
        }
        return super.getCompositePropertyName(propertyName);
    }

    @Override
    public void writeToItem(PropertysetItem newValues) {
        // TODO: get property with ID (live debug to get the name)
        Property prop = relatedFormItem.getItemProperty(this.propertyPrefix);

        RestEasyClient client = null;
        U2BService service = null;
        try {
            client = (RestEasyClient) restClientRegistry.getRestClient("youtube");
            service = client.getClientService(U2BService.class);
        } catch (RegistrationException e) {
            log.error("Failed to get a client for [" + U2BService.class.getName() + "] with: " + e.getMessage(), e);
        }
        if (service != null) {
            // call get videoId() with that prop
            // do the rest
            String id = getVideoId(prop);
            String key = u2bModule.getGoogleKey();
            JsonNode response = service.meta(id, "snippet", key);
            try {
                if (response.get("items").getElements().hasNext()) {
                    JsonNode videoItem = response.get("items").getElements().next();
                    JsonNode snippet = videoItem.get("snippet");

                    setNewValue(relatedFormItem, snippet, "description");
                    setNewValue(relatedFormItem, snippet, "title");

                    setNewValue(relatedFormItem, snippet, "publishedAt");

                    Iterator<Entry<String, JsonNode>> thumbs = videoItem.get("snippet").get("thumbnails").getFields();

                    String thumbsName = getCompositePropertyName("Thumbs");
                    JcrNodeAdapter thumbsParent = getOrCreateChildItem((JcrNodeAdapter) relatedFormItem, thumbsName);

                    while (thumbs.hasNext()) {
                        Entry<String, JsonNode> entry = thumbs.next();
                        JcrNodeAdapter thumbChild = getOrCreateChildItem(thumbsParent, entry.getKey());
                        String propId = "url";
                        thumbChild.removeItemProperty(propId);
                        thumbChild.addItemProperty(propId, new ObjectProperty(entry.getValue().get(propId).getTextValue()));
                        propId = "width";
                        thumbChild.removeItemProperty(propId);
                        thumbChild.addItemProperty(propId, new ObjectProperty(entry.getValue().get(propId).getLongValue()));
                        propId = "height";
                        thumbChild.removeItemProperty(propId);
                        thumbChild.addItemProperty(propId, new ObjectProperty(entry.getValue().get(propId).getLongValue()));
                        thumbsParent.addChild(thumbChild);
                    }

                    ((JcrNodeAdapter) relatedFormItem).addChild(thumbsParent);

                }
            } catch (Exception e) {
                log.error("Failed to parse the video metadata.", e);
            }

            response = service.meta(id, "contentDetails", key);
            try {
                if (response.get("items").getElements().hasNext()) {
                    JsonNode videoItem = response.get("items").getElements().next();
                    setNewValue(relatedFormItem, videoItem.get("contentDetails"), "duration");
                    setNewValue(relatedFormItem, videoItem.get("contentDetails"), "definition");
                }
            } catch (Exception e) {
                log.error("Failed to parse the video duration.", e);
            }

        }
    }

    private void setNewValue(Item relatedFormItem, JsonNode snippet, String fieldName) {
        String value = snippet.get(fieldName).getTextValue();
        String compositePropertyName = getCompositePropertyName(StringUtils.capitalize(fieldName));
        relatedFormItem.addItemProperty(compositePropertyName, new ObjectProperty(value));
    }

    public String getVideoId(Property prop) {
        if (prop == null) {
            throw new NullPointerException("Video is not set or name of the required field for this dialog is not correctly configured.");
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
     * Return the child item containing the properties (displayed in the multiField).
     */
    private JcrNodeAdapter getOrCreateChildItem(JcrNodeAdapter parent, String childNodeName) throws RepositoryException {

        JcrNodeAdapter child = null;
        Node rootNode = parent.getJcrItem();
        if (rootNode.hasNode(childNodeName)) {
            child = new JcrNodeAdapter(rootNode.getNode(childNodeName));
            Node childNode = new JCRMgnlPropertiesFilteringNodeWrapper(rootNode.getNode(childNodeName));
            PropertyIterator iterator = childNode.getProperties();
            while (iterator.hasNext()) {
                // Make sure we populate the adapter with existing JCR properties.
                child.getItemProperty(iterator.nextProperty().getName());
            }
        } else {
            child = new JcrNewNodeAdapter(rootNode, NodeTypes.ContentNode.NAME, childNodeName);
        }
        parent.addChild(child);
        return child;
    }

    @Override
    public PropertysetItem readFromItem() {
        PropertysetItem supsi = super.readFromItem();
        try {
            String thumbsName = getCompositePropertyName("Thumbs");
            JcrNodeAdapter thumbsParent = getOrCreateChildItem((JcrNodeAdapter) relatedFormItem, thumbsName);
            PropertysetItem psi = new PropertysetItem();

            // Get a list of childNodes
            List<Node> childNodes = NodeUtil.asList(NodeUtil.getNodes(thumbsParent.getJcrItem()));
            int position = 0;
            for (Node child : childNodes) {
                PropertysetItem cpsi = new PropertysetItem();
                PropertyIterator cProps = new FilteringPropertyIterator(child.getProperties(), new JCRMgnlPropertyHidingPredicate());
                while (cProps.hasNext()) {
                    javax.jcr.Property cProp = cProps.nextProperty();
                    cpsi.addItemProperty(cProp.getName(), new DefaultProperty<String>(String.class, cProp.getString()));
                }
                psi.addItemProperty(position, new DefaultProperty<PropertysetItem>(PropertysetItem.class, cpsi));
                position += 1;
            }
            supsi.addItemProperty("Thumbs", new DefaultProperty<PropertysetItem>(PropertysetItem.class, psi));
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return supsi;
    }

}