package value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LispList {
    private final List<Value<?>> list;
    private final Map<Value<?>, Value<?>> propertyList;

    public LispList(List<Value<?>> list) {
        this.list = list;

        propertyList = new HashMap<>();
        for(int i=0; i<list.size(); i+=2) {
            Value<?> property = list.get(i);
            Value<?> propertyValue = list.size() > i+1 ? list.get(i+1) : null;
            propertyList.put(property, propertyValue);
        }
    }

    public List<Value<?>> getList() {
        return list;
    }

    public Map<Value<?>, Value<?>> getPropertyList() {
        return propertyList;
    }
}
