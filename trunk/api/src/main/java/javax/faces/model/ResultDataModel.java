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

import javax.servlet.jsp.jstl.sql.Result;
import java.util.SortedMap;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ResultDataModel extends DataModel
{
    // FIELDS
    private int _rowIndex = -1;
    private Result _data;

    // CONSTRUCTORS
    public ResultDataModel()
    {
        super();
    }

    public ResultDataModel(Result result)
    {
        if (result == null) throw new NullPointerException("result");
        setWrappedData(result);
    }

    // METHODS
    public int getRowCount()
    {
        if (getRows() == null)
        {
            return -1;
        }
        return getRows().length;
    }

    public Object getRowData()
    {
        if (getRows() == null)
        {
            return null;
        }
        if (!isRowAvailable())
        {
            throw new IllegalArgumentException("row is unavailable");
        }
        return getRows()[_rowIndex];
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
        if (getRows() == null)
        {
            return false;
        }
        return _rowIndex >= 0 && _rowIndex < getRows().length;
    }

    public void setRowIndex(int rowIndex)
    {
        if (rowIndex < -1)
        {
            throw new IllegalArgumentException("illegal rowIndex " + rowIndex);
        }
        int oldRowIndex = _rowIndex;
        _rowIndex = rowIndex;
        if (getRows() != null && oldRowIndex != _rowIndex)
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

    private SortedMap[] getRows()
    {
        if(_data==null)
            return null;

        return _data.getRows();
    }

    public void setWrappedData(Object data)
    {
        if (data == null)
        {
            setRowIndex(-1);
        }
        else
        {
            _data = ((Result)data);
            setRowIndex(0);
        }
    }

}
