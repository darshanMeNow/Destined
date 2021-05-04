package com.example.destined


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


class HomeFragment : Fragment(),RecyclerViewClickListener{
    private lateinit var viewModel: LocationsViewModel
    private val adapter = AuthorsAdapter()
    private var fusedLocationProviderClient: FusedLocationProviderClient?=null
    private var location : MutableLiveData<Location> = MutableLiveData()
    //private var btnGo=go_btn



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        viewModel = ViewModelProvider(this).get(LocationsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapter.listener=this
        recycler_view_authors.adapter = adapter
        viewModel.fetchLoc()
        viewModel.getRealtimeUpdates()
        viewModel.result.observe(viewLifecycleOwner, Observer {

            val message = if (it == null) {
                getString(R.string.loc_added)
            } else {
                getString(R.string.errors, it.message)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })
        viewModel.locashans.observe(viewLifecycleOwner, Observer {
            adapter.setAuthors(it)
        })
        viewModel.author.observe(viewLifecycleOwner, Observer {
            adapter.addAuthor(it)
        })


        var finalCityName="x"
        var longVal="T1"
        var latVal="T2"
        var latv="a"
        var longv="b"
        search_it.setOnClickListener(object : View.OnClickListener {


            override fun onClick(view: View?) {
                val addr = et_place.text.toString()

                val loc = Locashan()
//                val loc=Locashan()
//                loc.name=addr
//                //loc.latit=
//                viewModel.addLoc(loc)

                //GeoLocation geoLocation=new GeoLocation();
                //geoLocation.getAddre(addr,getApplicationContext(),new GeoHandler());
                //tv_address.setText();
                val client = OkHttpClient()
                var url =
                        "https://forward-reverse-geocoding.p.rapidapi.com/v1/forward?format=json&city="
                url = url + addr
                val postf = "&polygon_threshold=0.0"
                url = url + postf
                val request = Request.Builder()
                        .url(url)
                        .get()
                        .addHeader(
                                "x-rapidapi-key",
                                "07a1438de1msha210a06e30e4aabp127974jsn7ca2f60d13f1"
                        )
                        .addHeader("x-rapidapi-host", "forward-reverse-geocoding.p.rapidapi.com")
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val myResponse = response.body()!!.string()
                                val cityIndex = myResponse.indexOf("display_name")
                                val osm_Index = myResponse.indexOf("osm_type")
                                val cityName = myResponse.substring(cityIndex + 14, osm_Index - 2)
                                val longindex = myResponse.indexOf("lon")
                                longVal = myResponse.substring(longindex + 6, longindex + 14)
                                //cityName = "$cityName $longVal".trimIndent()
                                val latindex = myResponse.indexOf("lat")
                                latVal = myResponse.substring(latindex + 6, latindex + 14)
                                //cityName = "$cityName $latVal".trimIndent()
                                finalCityName = cityName
                                launch(Dispatchers.Main) {
                                    tv_address.text = finalCityName
                                    tv_address_lat.text = latVal
                                    tv_address_long.text = longVal

                                }
                            }

                        }
                    }
                })
                Handler().postDelayed(
                        {
                            // This method will be executed once the timer is over
                            latv = tv_address_lat.text as String
                            longv = tv_address_long.text as String
                            loc.name = addr
                            loc.latit = latv
                            loc.longit = longv
                            viewModel.addLoc(loc)
                        },
                        5000 // value in milliseconds
                )


            }
        })
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(requireActivity())
        //go_btn manupulations
        go_btn.setOnClickListener {
            checkPermissions()
        }


    }
    private fun checkPermissions(){
        if(activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } !=PackageManager.PERMISSION_GRANTED){
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1) }
        }
        else{
            getLocation()
            Handler().postDelayed(
                    {
                        checkNmatch()
                    },
                    4000 // value in milliseconds
            )

        }
    }
    // using singleton pattern to get the locationProviderClient
    fun getInstance(appContext: Context): FusedLocationProviderClient{
        if(fusedLocationProviderClient == null)
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)
        return fusedLocationProviderClient!!
    }
    var curlat="1"
    var curlong="2"
    @SuppressLint("MissingPermission")
    fun getLocation() : LiveData<Location> {
        fusedLocationProviderClient!!.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    location.value = loc
                    if (loc != null) {
                        curlat=loc.latitude.toString()
                        curlong=loc.longitude.toString()
                        curnt_address_lat.text= curlat
                        curnt_address_long.text= curlong
                    }
                }

        return location
    }
    private fun checkNmatch(){
        viewModel.locashans.observe(viewLifecycleOwner, Observer {
            val firstlat = adapter.getAuthors()[0].latit
            val firstlong = adapter.getAuthors()[0].longit

            //System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "K") + " Kilometers\n");

//            val dist=f[0].toString()
            try {
                if (firstlat != null) {
                    if (firstlong != null) {
                        val dist = distance(curlat.toDouble(), curlong.toDouble(), firstlat.toDouble(), firstlong.toDouble(), "K")
                        Log.i("Distance", dist.toString())
                        //set your distance here --2.0 km here
                        if(dist<2.0){
                            viewModel.deleteAuthor(adapter.getAuthors()[0])
                            //adapter.getAuthors()[0].id=null
                        }
                    }
                }
            } catch (e: NumberFormatException) {
                Log.i("Error dist", "EXcePtion")
            }



            Log.i("listIs============", firstlat.toString())
            Log.i("listIs==============", firstlong.toString())


        })
    }
    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double, unit: String): Double {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0
        } else {
            val theta = lon1 - lon2
            var dist = sin(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(Math.toRadians(theta))
            dist = acos(dist)
            dist = Math.toDegrees(dist)
            dist = dist * 60 * 1.1515
            if (unit == "K") {
                dist = dist * 1.609344
            } else if (unit == "N") {
                dist = dist * 0.8684
            }
            return dist
        }
    }
//    @SuppressLint("MissingPermission")
//    private fun getLocations(){
//        fusedLocationProviderClient.lastLocation?.addOnSuccessListener {
//            if(it!=null)it.apply{
//                val latThing=it.latitude
//                val longThing=it.longitude
//                curnt_address_lat.text= latThing.toString()
//                curnt_address_long.text= longThing.toString()
//            }
//        }
//
//
//    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode==1){
            if(grantResults.isNotEmpty()&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } !=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show()
                    getLocation()
                }
                else{
                    Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRecyclerViewItemClicked(view: View, author: Locashan) {
        when (view.id) {
            R.id.button_edit -> {
                EditAuthorDialogFragment(author).show(childFragmentManager, "")
            }
            R.id.button_delete -> {
                AlertDialog.Builder(requireContext()).also {
                    it.setTitle(getString(R.string.delete_confirmation))
                    it.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                        viewModel.deleteAuthor(author)
                    }
                }.create().show()
            }
        }
    }
}