/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellectualcrafters.jnbt;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code TAG_String} tag.
 */
public final class StringTag extends Tag {

    private final String value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public StringTag(String value) {
        super();
        checkNotNull(value);
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name the name of the tag
     * @param value the value of the tag
     */
    public StringTag(String name, String value) {
        super(name);
        checkNotNull(value);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        String name = getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_String" + append + ": " + value;
    }

}
