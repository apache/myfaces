/*
 * Copyright 2004 The Apache Software Foundation.
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

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ArrayDataModel extends DataModel
{
	// FIELDS
    private int _rowIndex = -1;
    private Object[] _data;

	// CONSTRUCTORS
	public ArrayDataModel()
	{
		super();
	}

	public ArrayDataModel(Object[] array)
	{
        if (array == null) throw new NullPointerException("array");
		setWrappedData(array);
	}

	// METHODS
	public int getRowCount()
	{
        if (_data == null)
        {
            return -1;
        }
        return _data.length;
	}

	public Object getRowData()
	{
		if (_data == null)
        {
            return null;
        }
        if (!isRowAvailable())
        {
            throw new IllegalArgumentException("row is unavailable");
        }
		return _data[_rowIndex];
	}

	public int getRowIndex()
	{
		return _rowIndex;
	}

	public Object getWrappedData()
	{
		return _data;
	}

	public boolean isRowAvailable()
	{
		if (_data == null)
        {
            return false;
        }
        return _rowIndex >= 0 && _rowIndex < _data.length;
	}

	public void setRowIndex(int rowIndex)
	{
		if (rowIndex < -1)
        {
            throw new IllegalArgumentException("illegal rowIndex " + rowIndex);
        }
        int oldRowIndex = _rowIndex;
		_rowIndex = rowIndex;
        if (_data != null && oldRowIndex != _rowIndex)
        {
            Object data = isRowAvailable() ? getRowData() : null;
            DataModelEvent event = new DataModelEvent(this, _rowIndex, data);
            DataModelListener[] listeners = getDataModelListeners();
            for (int i = 0; i < listeners.length; i++)
            {
                listeners[i].rowSelected(event);
            }
        }
	}

	public void setWrappedData(Object data)
	{
        _data = (Object[])data;
		int rowIndex = _data != null ? 0 : -1;
        setRowIndex(rowIndex);
	}

}
