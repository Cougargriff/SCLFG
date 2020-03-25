package org.griffin.sclfg.View.Tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.griffin.sclfg.R

class ListFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tabSearch, container)

        /* Setup Fragment View here */

        return view
    }
}