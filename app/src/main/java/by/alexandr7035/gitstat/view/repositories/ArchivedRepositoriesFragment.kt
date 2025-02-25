package by.alexandr7035.gitstat.view.repositories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.alexandr7035.gitstat.R
import by.alexandr7035.gitstat.databinding.FragmentRepositoriesRecyclerBinding
import by.alexandr7035.gitstat.view.repositories.filters.RepositoriesListFiltersHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchivedRepositoriesFragment : Fragment() {

    private val viewModel by navGraphViewModels<RepositoriesViewModel>(R.id.repositoriesListGraph) { defaultViewModelProviderFactory }
    private var binding: FragmentRepositoriesRecyclerBinding? = null
    private var adapter: RepositoriesAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRepositoriesRecyclerBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup adapter
        adapter = RepositoriesAdapter()
        binding!!.recycler.adapter = adapter
        binding!!.recycler.layoutManager = LinearLayoutManager(context)

        viewModel.getTabRefreshedLiveData().observe(viewLifecycleOwner, {
            // If current fragment
            // FIXME find better solution
            if (it == 1) {
                //binding!!.root.smoothScrollToPosition(0)
                binding!!.recycler.layoutManager!!.scrollToPosition(0)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        viewModel.getArchivedRepositoriesLiveData().observe(viewLifecycleOwner, { repos ->
            val filteredList = RepositoriesListFiltersHelper.getFilteredRepositoriesList(
                repos,
                viewModel.getRepositoriesFilters()
            )

            // When there are no repos at all
            if (repos.isEmpty()) {
                binding?.recycler?.visibility = View.GONE
                binding?.noReposStub?.visibility = View.VISIBLE
                binding?.noReposStubText?.text = getString(R.string.no_archived_repos_list)
            }
            // When there are repos but list is empty because of filters
            else if (repos.isNotEmpty() && filteredList.isEmpty()) {
                binding?.recycler?.visibility = View.GONE
                binding?.noReposStub?.visibility = View.VISIBLE
                binding?.noReposStubText?.text = getString(R.string.no_archived_repos_list_check_filters)
            }
            else {
                binding?.recycler?.visibility = View.VISIBLE
                binding?.noReposStub?.visibility = View.GONE
                adapter!!.setItems(filteredList)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}