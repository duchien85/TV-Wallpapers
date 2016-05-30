package rs.pedjaapps.moviewallpapers.model;

import java.util.ArrayList;
import java.util.List;

public class Page<T extends TypeListItem.IListItem>
{
    public List<TypeListItem<T>> items;
    public int page = 0, count;
    public boolean next = true;

    public Page()
    {
        items = new ArrayList<>();
    }
}