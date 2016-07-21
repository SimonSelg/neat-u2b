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
package com.neatresults.mgnltweaks.neatu2b;

/**
 * This class is optional and represents the configuration for the neat-u2b module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/neat-u2b</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class NeatU2b {

    private String googleKey;

    public String getGoogleKey() {
        return googleKey;
    }

    public void setGoogleKey(String googleKey) {
        this.googleKey = googleKey;
    }
}
