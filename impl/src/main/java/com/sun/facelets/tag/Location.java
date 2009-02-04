/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.tag;

/**
 * An object that represents the Location of a Tag or TagAttribute in a Facelet file.
 * 
 * @see com.sun.facelets.tag.Tag
 * @see com.sun.facelets.tag.TagAttribute
 * @author Jacob Hookom
 * @version $Id: Location.java,v 1.4 2008/07/13 19:01:35 rlubke Exp $
 */
public final class Location
{

    private final String path;

    private final int line;

    private final int column;

    public Location(String path, int line, int column)
    {
        this.path = path;
        this.line = line;
        this.column = column;
    }

    /**
     * Estimated character column
     * 
     * @return character column
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * Line this is located at
     * 
     * @return link this is located at
     */
    public int getLine()
    {
        return line;
    }

    /**
     * File path to this location
     * 
     * @return file path
     */
    public String getPath()
    {
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return path + " @" + this.line + "," + this.column;
    }
}
