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

import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * Expands single field with youtube video id/link to set of fields containing also metadata about the video.
 */
public class U2BFieldTransformer extends BasicTransformer<PropertysetItem> {
    private static final Logger log = LoggerFactory.getLogger(U2BFieldTransformer.class);
    protected List<String> fieldsName;
    private PropertysetItem items;

    @Inject
    public U2BFieldTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName, I18NAuthoringSupport i18NAuthoringSupport) {
        super(relatedFormItem, definition, type);
        this.fieldsName = fieldsName;
    }

    /**
     * This transformer's write implementation writes just main item. Metadata will write themselves.
     */
    @Override
    public void writeToItem(PropertysetItem newValue) {
        Property p = getOrCreateProperty(type);
        p.setValue(newValue);
    }

    /**
     * Returns a representation of the child items as a {@link PropertysetItem}; this is merely a map whose keys are the configured names of the sub-fields, and whose values are the child items, wrapped as {@link ObjectProperty ObjectProperties}.
     */
    @Override
    public PropertysetItem readFromItem() {
        // Only read it once
        if (items != null) {
            return items;
        }
        items = new PropertysetItem();
        for (String fieldName : fieldsName) {
            items.addItemProperty(fieldName, new ObjectProperty<Item>(relatedFormItem));
        }
        return items;
    }
}