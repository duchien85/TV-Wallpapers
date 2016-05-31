package rs.pedjaapps.moviewallpapers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import rs.pedjaapps.moviewallpapers.fragment.SearchFragment;

public class SearchActivity extends AppCompatActivity implements TextWatcher
{
    private SearchFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentSearch);
        mFragment.reuseRequest = false;

        EditText etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }

    @Override
    public void afterTextChanged(Editable s)
    {
        if(s.length() < 3)
            mFragment.query = null;
        else
            mFragment.query = s.toString();
        mFragment.onRefresh();
    }
}
