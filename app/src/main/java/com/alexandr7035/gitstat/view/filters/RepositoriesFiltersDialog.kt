package com.alexandr7035.gitstat.view.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alexandr7035.gitstat.R
import com.alexandr7035.gitstat.core.Language
import com.alexandr7035.gitstat.databinding.FiltersDialogBinding
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class RepositoriesFiltersDialog(
    private val currentFilters: ReposFilters,
    private val filtersUpdateObserver: FiltersUpdateObserver,
    // FIXME
    private val languages: List<Language>): BottomSheetDialogFragment() {

    private var binding: FiltersDialogBinding? = null
    private var checkedLanguages = HashSet<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Allow to fill full screen
        // Also disable dragging (for recyclerview correct work)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = false
        }

        binding = FiltersDialogBinding.inflate(inflater, container, false)
        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load current filters settings
        setupFiltersViews()

        val adapter = LanguagesAdapter(checkedLanguages)
        binding!!.languagesFilterRecyclerView.adapter = adapter
        binding!!.languagesFilterRecyclerView.layoutManager = FlexboxLayoutManager(context)
        adapter.setItems(languages)

        binding!!.applyButton.setOnClickListener {

            val newFilters = ReposFilters()

            when (binding!!.includeRepositoriesRadioGroup.checkedRadioButtonId) {
                binding!!.includeAllBtn.id -> {
                    newFilters.filterPrivacy = ReposFilters.FilterPrivacy.ALL_REPOS
                }

                binding!!.includePrivateOnlyBtn.id -> {
                    newFilters.filterPrivacy = ReposFilters.FilterPrivacy.PRIVATE_REPOS_ONLY
                }

                binding!!.includePublicOnlyBtn.id -> {
                    newFilters.filterPrivacy = ReposFilters.FilterPrivacy.PUBLIC_REPOS_ONLY
                }
            }


            when (binding!!.sortingTypeRadioGroup.checkedRadioButtonId) {
                binding!!.sortByRepositoryNameBtn.id -> {
                    newFilters.sortingType = ReposFilters.SortingType.BY_REPO_NAME
                }

                binding!!.sortByCreationDateBtn.id -> {
                    newFilters.sortingType = ReposFilters.SortingType.BY_REPO_CREATION_DATE
                }
            }


            when (binding!!.sortingOrderRadioGroup.checkedRadioButtonId) {
                binding!!.sortAscendingBtn.id -> {
                    newFilters.sortingOrder = ReposFilters.SortingOrder.ASCENDING_MODE
                }

                binding!!.sortDescendingBtn.id -> {
                    newFilters.sortingOrder = ReposFilters.SortingOrder.DESCENDING_MODE
                }
            }

            newFilters.filterLanguages = adapter.getCheckedLanguages()

            filtersUpdateObserver.onFiltersUpdated(newFilters)
            dismiss()
        }


        // Clear buttons
        binding!!.clearVisibilityBtn.setOnClickListener {
            binding!!.includeRepositoriesRadioGroup.check(R.id.includeAllBtn)
        }

        binding!!.clearLanguagesBtn.setOnClickListener {
            adapter.clearCheckedLanguages()
        }
    }


    private fun setupFiltersViews() {
        when (currentFilters.filterPrivacy) {
            ReposFilters.FilterPrivacy.PRIVATE_REPOS_ONLY -> binding!!.includePrivateOnlyBtn.isChecked = true
            ReposFilters.FilterPrivacy.PUBLIC_REPOS_ONLY -> binding!!.includePublicOnlyBtn.isChecked = true
            ReposFilters.FilterPrivacy.ALL_REPOS -> binding!!.includeAllBtn.isChecked = true
        }

        when (currentFilters.sortingType) {
            ReposFilters.SortingType.BY_REPO_NAME -> binding!!.sortByRepositoryNameBtn.isChecked = true
            ReposFilters.SortingType.BY_REPO_CREATION_DATE -> binding!!.sortByCreationDateBtn.isChecked = true
        }

        when (currentFilters.sortingOrder) {
            ReposFilters.SortingOrder.ASCENDING_MODE -> binding!!.sortAscendingBtn.isChecked = true
            ReposFilters.SortingOrder.DESCENDING_MODE -> binding!!.sortDescendingBtn.isChecked = true
        }

        checkedLanguages = currentFilters.filterLanguages as HashSet<String>
    }

    interface FiltersUpdateObserver {
        fun onFiltersUpdated(filters: ReposFilters)
    }

}