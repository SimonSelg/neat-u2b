/**
 * This file Copyright (c) 2013-2016 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package com.neatresults.mgnltweaks.neatu2b.ui.form.field;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rest.client.registry.RestClientRegistry;
import info.magnolia.resteasy.client.RestEasyClient;
import info.magnolia.ui.form.config.TextFieldBuilder;
import info.magnolia.ui.form.field.CompositeField;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.CompositeFieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.transformer.multi.MultiValueChildNodeTransformer;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
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

import com.google.common.collect.ImmutableList;
import com.neatresults.mgnltweaks.neatu2b.NeatU2b;
import com.neatresults.mgnltweaks.neatu2b.restclient.U2BService;
import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Youtube Field based/built upon CompositeField.<br>
 * The Field values are handle by a configured {@link info.magnolia.ui.form.field.transformer.Transformer} dedicated to create/retrieve properties as {@link PropertysetItem}.<br>
 */
/**
 * U2BField.
 */
public class U2BField extends CompositeField {

    private static final Logger log = LoggerFactory.getLogger(U2BField.class);
    private RestClientRegistry restClientRegistry;
    private NeatU2b u2bModule;

    public U2BField(Definition definition, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider, Item relatedFieldItem, RestClientRegistry restClientRegistry, NeatU2b u2bModule) {
        super(definition, fieldFactoryFactory, i18nContentSupport, componentProvider, relatedFieldItem);
        this.u2bModule = u2bModule;
        this.restClientRegistry = restClientRegistry;
    }

    /**
     * Initialize the field. <br>
     * Create as many configured Field as we have related values already stored.
     */
    @Override
    protected void initFields(final PropertysetItem newValue) {
        root.removeAllComponents();
        final TextField id = createTextField("Id", newValue);
        root.addComponent(id);
        final TextField title = createTextField("Title", newValue);
        root.addComponent(title);

        final TextFieldDefinition def = new TextFieldBuilder("description").label("Description").rows(3).definition();
        final TextArea description = (TextArea) createLocalField(def, newValue.getItemProperty(def.getName()), false);
        newValue.addItemProperty(def.getName(), description.getPropertyDataSource());
        description.setNullRepresentation("");
        description.setWidth("100%");
        description.setNullSettingAllowed(true);
        root.addComponent(description);

        HorizontalLayout ddLine = new HorizontalLayout();
        final TextField publishedAt = createTextField("Published", newValue);
        ddLine.addComponent(publishedAt);
        final TextField duration = createTextField("Duration", newValue);
        ddLine.addComponent(duration);
        ddLine.addComponent(createTextField("Definition", newValue));

        Button fetchButton = new Button("Fetch metadata");
        fetchButton.addStyleName("magnoliabutton");
        fetchButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {

                String idStr = id.getValue();
                // extract id from url when whole url is passed
                if (idStr.startsWith("http")) {
                    idStr = StringUtils.substringBefore(StringUtils.substringAfter(idStr, "?v="), "&");
                }

                U2BService service = null;
                try {
                    RestEasyClient client = (RestEasyClient) restClientRegistry.getRestClient("youtube");
                    service = client.getClientService(U2BService.class);
                } catch (RegistrationException e) {
                    log.error("Failed to get a client for [" + U2BService.class.getName() + "] with: " + e.getMessage(), e);
                }
                if (service != null) {
                    String key = u2bModule.getGoogleKey();
                    JsonNode response = service.meta(idStr, "snippet", key);
                    try {
                        if (response.get("items").getElements().hasNext()) {
                            JsonNode videoItem = response.get("items").getElements().next();
                            String descriptionStr = videoItem.get("snippet").get("description").getTextValue();
                            newValue.getItemProperty("description").setValue(descriptionStr);
                            String titleStr = videoItem.get("snippet").get("title").getTextValue();
                            newValue.getItemProperty("title").setValue(titleStr);
                            Iterator<Entry<String, JsonNode>> thumbs = videoItem.get("snippet").get("thumbnails").getFields();
                            while (thumbs.hasNext()) {
                                Entry<String, JsonNode> entry = thumbs.next();
                                newValue.getItemProperty(entry.getKey() + "Url").setValue(entry.getValue().get("url").getTextValue());
                                newValue.getItemProperty(entry.getKey() + "Width").setValue("" + entry.getValue().get("width").getLongValue());
                                newValue.getItemProperty(entry.getKey() + "Height").setValue("" + entry.getValue().get("height").getLongValue());
                            }
                            String publishedAtStr = videoItem.get("snippet").get("publishedAt").getTextValue();
                            newValue.getItemProperty("published").setValue(publishedAtStr);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse the video metadata.", e);
                    }

                    response = service.meta(idStr, "contentDetails", key);
                    try {
                        if (response.get("items").getElements().hasNext()) {
                            JsonNode videoItem = response.get("items").getElements().next();
                            String durationStr = videoItem.get("contentDetails").get("duration").getTextValue();
                            newValue.getItemProperty("duration").setValue(durationStr);
                            String definition = videoItem.get("contentDetails").get("definition").getTextValue();
                            newValue.getItemProperty("definition").setValue(definition);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse the video duration.", e);
                    }
                }
            }
        });

        ddLine.addComponent(fetchButton);
        ddLine.setWidth(100, Unit.PERCENTAGE);
        ddLine.setHeight(-1, Unit.PIXELS);
        ddLine.setComponentAlignment(fetchButton, Alignment.BOTTOM_RIGHT);
        root.addComponent(ddLine);

        PropertysetItem item = (PropertysetItem) getPropertyDataSource().getValue();
        root.addComponent(createEntryComponent("default", item), root.getComponentCount() - 1);
        root.addComponent(createEntryComponent("standard", item), root.getComponentCount() - 1);
        root.addComponent(createEntryComponent("medium", item), root.getComponentCount() - 1);
        root.addComponent(createEntryComponent("high", item), root.getComponentCount() - 1);
        root.addComponent(createEntryComponent("maxres", item), root.getComponentCount() - 1);
    }

    /**
     * Create a single element.<br>
     * This single element is composed of:<br>
     * - a configured field <br>
     * - a remove Button<br>
     */
    private Component createEntryComponent(String propertyId, PropertysetItem newValue) {
        String cappedId = StringUtils.capitalize(propertyId);
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setHeight(-1, Unit.PIXELS);
        TextField url = createTextField(cappedId + " Thumbnail", newValue, propertyId + "Url");
        layout.addComponent(url);

        TextField width = createTextField("Width", newValue, propertyId + "Width");
        layout.addComponent(width);
        TextField height = createTextField("Height", newValue, propertyId + "Height");
        layout.addComponent(height);
        layout.setExpandRatio(url, .7f);
        layout.setExpandRatio(width, .15f);
        layout.setExpandRatio(height, .15f);

        return layout;
    }

    private TextField createTextField(String label, PropertysetItem newValue) {
        return createTextField(label, newValue, StringUtils.uncapitalize(label));
    }

    private TextField createTextField(String label, PropertysetItem newValue, String id) {
        // TODO: i18n-ize
        final TextFieldDefinition def = new TextFieldBuilder(id).label(label).definition();
        final TextField field = (TextField) createLocalField(def, newValue.getItemProperty(id), false);
        newValue.addItemProperty(id, field.getPropertyDataSource());

        field.setNullRepresentation("");
        field.setWidth("100%");
        field.setNullSettingAllowed(true);
        return field;

    }

    /**
     * AnotherU2BFieldTransformer just another variant MultiValueChildNodeTransformer. It exists only due to assumption that IDs are numbers in extended class. Sad but true nonetheless.
     */
    public static class Transformer extends MultiValueChildNodeTransformer {

        private static final Logger log = LoggerFactory.getLogger(Transformer.class);

        @Inject
        public Transformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type) {
            super(relatedFormItem, definition, type);
        }

        @Override
        public void writeToItem(PropertysetItem newValue) {
            // TODO: i18n support
            String childNodeName = definePropertyName();
            try {
                // get the child item
                JcrNodeAdapter child = getOrCreateChildItem((JcrNodeAdapter) relatedFormItem, childNodeName);
                // Remove all old properties

                ImmutableList<Object> propertyIds = ImmutableList.copyOf(child.getItemPropertyIds());
                for (Object id : propertyIds) {
                    if (newValue.getItemProperty(id) == null) {
                        child.removeItemProperty(id);
                    }
                }
                // add all the new properties
                if (newValue != null) {
                    Iterator<?> it = newValue.getItemPropertyIds().iterator();
                    while (it.hasNext()) {
                        Object id = it.next();
                        child.addItemProperty(id.toString(), newValue.getItemProperty(id));
                    }
                }
            } catch (RepositoryException re) {
                log.warn("Not able to access the child node of '{}'", NodeUtil.getName(((JcrNodeAdapter) relatedFormItem).getJcrItem()));
            }
        }

        @Override
        public PropertysetItem readFromItem() {
            // i18n support
            String childNodeName = definePropertyName();
            PropertysetItem newValues = new PropertysetItem();
            // Get the child node containing the list of properties.
            try {
                JcrNodeAdapter child = getOrCreateChildItem((JcrNodeAdapter) relatedFormItem, childNodeName);
                // Populate
                if (!(child instanceof JcrNewNodeAdapter)) {
                    List<Object> ids = new ArrayList<Object>(child.getItemPropertyIds());
                    for (Object id : ids) {
                        newValues.addItemProperty(id, child.getItemProperty(id));
                    }
                }
            } catch (RepositoryException re) {
                log.warn("Not able to access the child node of '{}'", ((JcrNodeAdapter) relatedFormItem).getNodeName());
            }
            return newValues;
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

    }

    /**
     * Definition for outter field.
     */
    public static class Definition extends CompositeFieldDefinition {

        public Definition() {
            setTransformerClass(Transformer.class);
            setLayout(Layout.vertical);
        }
    }

    /**
     * Factory for outer field. TODO: i18nize properly ... affected by changes introduced in 5.4.2
     */
    public static class Factory extends CompositeFieldFactory<Definition> {

        private FieldFactoryFactory fieldFactoryFactory;
        private ComponentProvider componentProvider;
        private I18nContentSupport i18nContentSupport;
        private RestClientRegistry restClientRegistry;
        private NeatU2b u2bModule;

        @Inject
        public Factory(Definition definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider, RestClientRegistry restClientRegistry, NeatU2b u2bModule) {
            super(definition, relatedFieldItem, fieldFactoryFactory, i18nContentSupport, componentProvider);
            this.fieldFactoryFactory = fieldFactoryFactory;
            this.componentProvider = componentProvider;
            this.i18nContentSupport = i18nContentSupport;
            this.restClientRegistry = restClientRegistry;
            this.u2bModule = u2bModule;
        }

        @Override
        protected Field<PropertysetItem> createFieldComponent() {
            return new U2BField(definition, fieldFactoryFactory, i18nContentSupport, componentProvider, item, restClientRegistry, u2bModule);
        }
    }
}
