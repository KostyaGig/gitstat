package by.alexandr7035.gitstat.view.contributions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import by.alexandr7035.gitstat.R
import by.alexandr7035.gitstat.databinding.FragmentContributionsBinding
import by.alexandr7035.gitstat.extensions.navigateSafe
import by.alexandr7035.gitstat.extensions.setChartData
import by.alexandr7035.gitstat.extensions.setupHorizontalBarChart
import by.alexandr7035.gitstat.extensions.setupYAxisValuesForContributionTypes
import by.alexandr7035.gitstat.view.MainActivity
import by.alexandr7035.gitstat.view.contributions.plots.contributions_per_year.YearContributionsAdapter
import by.alexandr7035.gitstat.view.contributions.plots.contributions_rate.YearContributionRatesAdapter
import by.alexandr7035.gitstat.view.contributions.plots.contributions_types.RemoveThousandsSepFormatter
import by.alexandr7035.gitstat.view.contributions.plots.contributions_types.TypesLegendAdapter
import by.alexandr7035.gitstat.view.contributions.plots.contributions_types.model.ContributionTypesListToBarDataSetMapper
import by.alexandr7035.gitstat.view.contributions.plots.contributions_types.model.ContributionTypesListToLegendItemsMapper
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ContributionsFragment : Fragment() {

    private var binding: FragmentContributionsBinding? = null
    private val viewModel by viewModels<ContributionsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentContributionsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter for contributions count plot
        val yearContributionsAdapter = YearContributionsAdapter(this)
        binding?.yearsViewPager?.adapter = yearContributionsAdapter

        // Adapter for contribution rate plot
        val yearContributionsRateAdapter = YearContributionRatesAdapter(this)
        binding?.rateViewPager?.adapter = yearContributionsRateAdapter

        // Adapter for legend on contribution types plot
        val typesLegendAdapter = TypesLegendAdapter()
        binding?.contributionTypesLegendRecycler?.layoutManager = FlexboxLayoutManager(requireContext())
        binding?.contributionTypesLegendRecycler?.adapter = typesLegendAdapter

        // Update data
        viewModel.getContributionYearsLiveData().observe(viewLifecycleOwner, { years ->

            if (years != null) {

                if (years.isNotEmpty()) {

                    yearContributionsAdapter.setItems(years)

                    // Set to last position
                    binding?.yearsViewPager?.setCurrentItem(years.size - 1, false)
                    binding?.currentYearView?.text = years[years.size - 1].year.id.toString()

                    // Change year in card title when viewpager item changes
                    binding?.yearsViewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            Timber.d("Page changed callback")
                            binding?.currentYearView?.text = years[position].year.id.toString()
                        }
                    })

                    // Attach tablayout
                    TabLayoutMediator(binding!!.yearsTabLayout, binding!!.yearsViewPager) { tab, position ->
                    }.attach()

                }
            }
        })


        viewModel.getContributionDaysLiveData().observe(viewLifecycleOwner, { contributions ->
            if (contributions != null) {
                val totalContributions = contributions.sumOf { it.count }
                binding?.totalContributions?.text = totalContributions.toString()
            }
        })


        viewModel.getContributionYearsWithRatesLiveData().observe(viewLifecycleOwner, { rateYears ->

            if (rateYears != null) {

                if (rateYears.isNotEmpty()) {
                    yearContributionsRateAdapter.setItems(rateYears)

                    TabLayoutMediator(binding!!.rateTabLayout, binding!!.rateViewPager) { tab, position ->
                    }.attach()

                    // Set to last position
                    binding?.rateViewPager?.setCurrentItem(rateYears.size - 1, false)

                    // Set total contribution rate in header
                    binding?.contributionsRate?.text =
                        viewModel.getLastTotalContributionRateForYear(rateYears[rateYears.size - 1]).toString()
                }
            }
        })




        viewModel.getContributionTypesLiveData().observe(viewLifecycleOwner, { typesData ->

            if (typesData != null) {

                // Detect max value
                // FIXME find better solution
                val commits = typesData.sumOf { it.commitContributions }
                val issues = typesData.sumOf { it.issueContributions }
                val pullRequests = typesData.sumOf { it.pullRequestContributions }
                val reviews = typesData.sumOf { it.pullRequestReviewContributions }
                val repositories = typesData.sumOf { it.repositoryContributions }
                val unknown = typesData.sumOf { it.unknownContributions }

                // FIXME refactoring
                val maxValue = listOf(
                    commits,
                    issues,
                    pullRequests,
                    reviews,
                    repositories,
                    unknown
                ).maxByOrNull {
                    it
                } ?: 0


                // Setup chart
                binding?.contributionTypesChart?.setupHorizontalBarChart(RemoveThousandsSepFormatter())
                binding?.contributionTypesChart?.setExtraOffsets(10f,0f,30f,0f)

                // Chart axis
                binding?.contributionTypesChart?.axisLeft?.setupYAxisValuesForContributionTypes(maxValue)

                // Populate chart with data
                binding?.contributionTypesChart?.setChartData(ContributionTypesListToBarDataSetMapper.map(typesData, requireContext()))
                binding?.contributionTypesChart?.invalidate()

                // Update legend
                typesLegendAdapter.setItems(ContributionTypesListToLegendItemsMapper.map(typesData, requireContext()))
            }
        })

        binding?.drawerBtn?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawerMenu()
        }

        // Help icon for contribution rate
        binding?.contributionRateHelpIcon?.setOnClickListener {
            findNavController().navigateSafe(ContributionsFragmentDirections.actionGlobalInfoDialogFragment(
                getString(R.string.what_is_contribution_rate_title),
                getString(R.string.what_is_contribution_rate_text)
            ))
        }

        // Help icon for contribution rate
        binding?.contributionRateDynamicsHelpIcon?.setOnClickListener {
            findNavController().navigateSafe(ContributionsFragmentDirections.actionGlobalInfoDialogFragment(
                getString(R.string.contribution_rate_dynamics_help_title),
                getString(R.string.contribution_rate_dynamics_help_text)
            ))
        }


        binding?.toContributionsGridBtn?.setOnClickListener {
            findNavController().navigateSafe(ContributionsFragmentDirections.actionContributionsFragmentToContributionsGridFragment(2021))
        }
    }

}