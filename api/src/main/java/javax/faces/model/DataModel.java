/*
 * Copyright 2004,2005,2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces.model;

import java.util.ArrayList;
import java.util.List;

/**
  * Represents the data presented by a UIData component, together with
  * some state information about the currently selected row within the
  * datalist for use by listeners on UIData components. This class allows
  * managed bean code to avoid binding directly to UIData components for
  * typical uses.
  * <p> 
  * Note that DataModel and its standard subclasses are not serializable,
  * as there is no state in a DataModel object itself that needs to be
  * preserved between render and restore-view. UIData components therefore
  * do not store their DataModel when serialized; they just evaluate their
  * "value" EL expression to refetch the object during the 
  * apply-request-values phase.
  * <p>
  * Because DataModel is not serializable, any managed bean that needs to
  * be serialized and which has a member of type DataModel should therefore
  * mark that member transient. If there is a need to preserve the datalist
  * contained within the DataModel then ensure a reference to that list is
  * stored in a non-transient member, or use a custom serialization method
  * that explicitly serializes dataModel.getWrappedData.
  *  
  * See Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a> for more.
  * 
  * @author Thomas Spiegl (latest modification by $Author$)
  * @version $Revision$ $Date$
*/
public abstract class DataModel
{
    // FIELDS
    private List<DataModelListener> _listeners;

    // METHODS
    public void addDataModelListener(DataModelListener listener)
    {
        if (listener == null) throw new NullPointerException("listener");
        if (_listeners == null)
        {
            _listeners = new ArrayList<DataModelListener>();
        }
        _listeners.add(listener);
    }

    public DataModelListener[] getDataModelListeners()
    {
        if (_listeners == null)
        {
            return new DataModelListener[0];
        }
        return _listeners.toArray(new DataModelListener[_listeners.size()]);
    }

    /**
     * Return the number of rows of data available. 
     * <p>
     * If the number of rows of data available is not known then -1 is returned.
     * This may happen for DataModels that wrap sources of data such as 
     * java.sql.ResultSet that provide an iterator to access the "next item"
     * rather than a fixed-size collection of data.
     */
    abstract public int getRowCount();

    /**
     * Return the object associated with the current row index.
     * <p>
     * Method isRowAvailable may be called before attempting to access
     * this method, to ensure that the data is available.
     * 
     * @throws RuntimeException subclass of some kind if the current row index
     * is not within the range of the current wrappedData property.
     */
    abstract public Object getRowData();

    /**
     * Get the current row index.
     */
    abstract public int getRowIndex();

    /**
     * Get the entire collection of data associated with this component. Note that
     * the actual type of the returned object depends upon the concrete
     * subclass of DataModel; the object will represent an "ordered sequence
     * of components", but may be implemented as an array, java.util.List,
     * java.sql.ResultSet or other similar types. 
     */
    abstract public Object getWrappedData();

    /**
     * Returns true if a call to getRowData will return a valid object.
     */
    abstract public boolean isRowAvailable();

    public void removeDataModelListener(DataModelListener listener)
    {
        if (listener == null) throw new NullPointerException("listener");
        if (_listeners != null)
        {
            _listeners.remove(listener);
        }
    }

    /**
     * Set the current row index. This affects the behaviour of the
     * getRowData method in particular.
     * 
     * Parameter rowIndex may be -1 to indicate "no row", or may be a value
     * between 0 and getRowCount()-1. 
     */
    abstract public void setRowIndex(int rowIndex);

    /**
     * Set the entire list of data associated with this component. Note that
     * the actual type of the provided object must match the expectations
     * of the concrete subclass of DataModel. See getWrappedData.
     */
    abstract public void setWrappedData(Object data);
}
