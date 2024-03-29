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
 * The {@code TAG_Int_Array} tag.
 */
public final class IntArrayTag extends Tag {

    private final int[] value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public IntArrayTag(int[] value) {
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
    public IntArrayTag(String name, int[] value) {
        super(name);
        checkNotNull(value);
        this.value = value;
    }

    @Override
    public int[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder hex = new StringBuilder();
        for (int b : value) {
            String hexDigits = Integer.toHexString(b).toUpperCase();
            if (hexDigits.length() == 1) {
                hex.append("0");
            }
            hex.append(hexDigits).append(" ");
        }
        String name = getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Int_Array" + append + ": " + hex;
    }

}
