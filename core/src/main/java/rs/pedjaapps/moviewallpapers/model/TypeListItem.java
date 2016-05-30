package rs.pedjaapps.moviewallpapers.model;

/**
 * Created by pedja on 29.10.14. 11.26.
 * This class is part of the Hub2Date
 * Copyright © 2014 ${OWNER}
 *
 * Wrapper for list item used in paged loading.
 */
public class TypeListItem<I extends TypeListItem.IListItem> implements Comparable<TypeListItem<I>>
{
    public static final int VIEW_TYPE_LOADER = 0;

    private final I item;

    public TypeListItem(I item)
    {
        this.item = item;
    }

    public TypeListItem()
    {
        this.item = null;
    }

    public int getViewType()
    {
        if(item == null)
        {
            return VIEW_TYPE_LOADER;
        }
        else
        {
            if(item.getViewType() < 1)throw new IllegalArgumentException("IListItem getViewType must return number > 0");
            return item.getViewType();
        }
    }

    public I getItem()
    {
        return item;
    }

    @Override
    public int compareTo(TypeListItem<I> i)
    {
        if(item == null || !(item instanceof Comparable))return 0;
        return ((Comparable)item).compareTo(i.item);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeListItem<?> that = (TypeListItem<?>) o;

        return !(item == null || that.item == null) && item.getId() == that.item.getId();

    }

    @Override
    public int hashCode()
    {
        return item != null ? (int) (item.getId() ^ (item.getId() >>> 32)) : 0;
    }

    public interface IListItem
    {
        int getViewType();
        long getId();
    }
}
