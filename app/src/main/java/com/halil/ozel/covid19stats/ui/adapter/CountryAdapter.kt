package com.halil.ozel.covid19stats.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.halil.ozel.covid19stats.api.CoronaApi.retrofitInstance
import com.halil.ozel.covid19stats.api.CoronaService
import com.halil.ozel.covid19stats.data.CountriesResponse
import com.halil.ozel.covid19stats.databinding.CountryRowBinding
import com.halil.ozel.covid19stats.ui.activity.DetailActivity
import com.halil.ozel.covid19stats.ui.adapter.CountryAdapter.CountryHolder
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CountryAdapter : RecyclerView.Adapter<CountryHolder>(), Filterable {
    private var countriesList: List<CountriesResponse>? = null
    private var countriesListed: List<CountriesResponse>? = null
    private var context: Context? = null
    fun setCountryList(context: Context?, countriesList: List<CountriesResponse>) {
        this.context = context
        if (this.countriesList == null) {
            this.countriesList = countriesList
            countriesListed = countriesList
            notifyItemChanged(0, countriesListed!!.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return this@CountryAdapter.countriesList!!.size
                }

                override fun getNewListSize(): Int {
                    return countriesList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return this@CountryAdapter.countriesList!![oldItemPosition].country === countriesList[newItemPosition].country
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val newMovie = this@CountryAdapter.countriesList!![oldItemPosition]
                    val oldMovie = countriesList[newItemPosition]
                    return newMovie.country === oldMovie.country
                }
            })
            this.countriesList = countriesList
            countriesListed = countriesList
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryHolder {
        val view = CountryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryHolder(view)
    }

    override fun onBindViewHolder(holder: CountryHolder, position: Int) {
        holder.binding.tvCountryDeath.text = "Total Death : " + countriesListed!![position].deaths
        holder.binding.tvCountryName.text = countriesListed!![position].country
        Picasso.with(context).load(countriesListed!![position].countryInfo!!.flag)
            .into(holder.binding.ivCountryPoster)
        holder.itemView.setOnClickListener { view ->
            val coronaService = retrofitInstance!!.create(CoronaService::class.java)
            val call = coronaService.getCountryInfo(countriesListed!![position].country)
            call!!.enqueue(object : Callback<CountriesResponse?> {
                override fun onResponse(
                    call: Call<CountriesResponse?>,
                    response: Response<CountriesResponse?>
                ) {
                    val intent = Intent(view.context, DetailActivity::class.java)
                    if (response.body() != null) {
                        intent.putExtra("country", response.body()!!.country)
                        intent.putExtra("todayCase", response.body()!!.todayCases)
                        intent.putExtra("todayDeath", response.body()!!.todayDeaths)
                        intent.putExtra("flag", response.body()!!.countryInfo!!.flag)
                        intent.putExtra("cases", response.body()!!.cases)
                        intent.putExtra("deaths", response.body()!!.deaths)
                        intent.putExtra("tests", response.body()!!.tests)
                        intent.putExtra("recovered", response.body()!!.recovered)
                    }
                    view.context.startActivity(intent)
                }

                override fun onFailure(call: Call<CountriesResponse?>, t: Throwable) {
                    t.message?.let { Log.d("Error", it) }
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return if (countriesList != null) {
            countriesListed!!.size
        } else {
            0
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                countriesListed = if (charString.isEmpty()) {
                    countriesList
                } else {
                    val filteredList: MutableList<CountriesResponse> = ArrayList()
                    for (movie in countriesList!!) {
                        if (movie.country!!.lowercase(Locale.getDefault())
                                .contains(charString.toLowerCase())
                        ) {
                            filteredList.add(movie)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = countriesListed
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                countriesListed = filterResults.values as ArrayList<CountriesResponse>
                notifyDataSetChanged()
            }
        }
    }

    inner class CountryHolder(val binding: CountryRowBinding) :
        RecyclerView.ViewHolder(binding.root)
}