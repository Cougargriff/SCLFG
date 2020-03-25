package org.griffin.sclfg.View.Tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.griffin.sclfg.R

class SearchFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tab_search, container, false)

        /* Setup Fragment View here */

        return view
    }
}