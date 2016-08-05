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

import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;

import java.util.LinkedList;
import java.util.List;

/**
 * Definition for youtube field.
 */
public class U2BFieldDefinition extends CompositeFieldDefinition {

    private LinkedList<ConfiguredFieldDefinition> fields = new LinkedList<>();

    public U2BFieldDefinition() {
        setTransformerClass(U2BFieldTransformer.class);
        setLayout(Layout.vertical);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        this.fields.clear();
    }

    @Override
    public List<ConfiguredFieldDefinition> getFields() {
        if (fields.isEmpty()) {
            TextFieldDefinition mainField = new TextFieldDefinition();
            mainField.setName("Id");
            mainField.setLabel("Video URL or Id");
            fields.add(mainField);
            TextFieldDefinition title = new TextFieldDefinition();
            title.setName("Title");
            title.setLabel("Title");
            fields.add(title);
            TextFieldDefinition description = new TextFieldDefinition();
            description.setName("Description");
            description.setLabel("Description");
            description.setRows(4);
            fields.add(description);
            TextFieldDefinition duration = new TextFieldDefinition();
            duration.setName("Duration");
            duration.setLabel("Duration");
            fields.add(duration);
            TextFieldDefinition definition = new TextFieldDefinition();
            definition.setName("Definition");
            definition.setLabel("Definition");
            fields.add(definition);

            MultiValueFieldDefinition thumbs = new MultiValueFieldDefinition();
            thumbs.setName("Thumbs");
            thumbs.setLabel("Thumbs");
            thumbs.setButtonSelectAddLabel("Add");
            // thumbs.setReadOnly(true);
            CompositeFieldDefinition thumb = new CompositeFieldDefinition();
            thumb.setLayout(Layout.vertical);
            thumb.setName("thumb");
            thumb.setLabel("Thumbnail");
            TextFieldDefinition width = new TextFieldDefinition();
            width.setName("width");
            width.setLabel("Width");
            thumb.addField(width);
            TextFieldDefinition height = new TextFieldDefinition();
            height.setName("height");
            height.setLabel("Height");
            thumb.addField(height);
            TextFieldDefinition url = new TextFieldDefinition();
            url.setName("url");
            url.setLabel("URL");
            thumb.addField(url);
            thumbs.setField(thumb);
            fields.add(thumbs);
            super.setFields(fields);
        }
        return super.getFields();
    }
}
