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
package com.neatresults.mgnltweaks.neatu2b.restclient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;

/**
 * Service to provide info about youtube videos.
 */
public interface U2BService {
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    JsonNode meta(@QueryParam("id") String id, @QueryParam("part") String part, @QueryParam("key") String key);
}