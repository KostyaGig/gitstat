package by.alexandr7035.gitstat.view.core

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.alexandr7035.gitstat.databinding.FragmentWebViewBinding


class WebViewFragment : Fragment() {

    private var binding: FragmentWebViewBinding? = null
    private val safeArgs by navArgs<WebViewFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.toolbar?.title = safeArgs.toolbarTitle
        binding?.toolbar?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding?.webView?.settings?.javaScriptEnabled = true
        loadPage()

        binding?.webView?.webViewClient = object: WebViewClient() {

            // Needed because onPageCommitVisible may be called after onReceivedError
            private var haveErrors: Boolean = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                haveErrors = false

                binding?.webView?.visibility = View.GONE
                binding?.progressView?.visibility = View.VISIBLE
                binding?.errorCard?.visibility = View.GONE
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                binding?.progressView?.visibility = View.GONE

                if (haveErrors) {
                    binding?.webView?.visibility = View.GONE
                    binding?.errorCard?.visibility = View.VISIBLE
                }
                else {
                    binding?.webView?.visibility = View.VISIBLE
                    binding?.errorCard?.visibility = View.GONE
                }

            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)

                haveErrors = true

                binding?.webView?.visibility = View.INVISIBLE
                binding?.progressView?.visibility = View.GONE
                binding?.errorCard?.visibility = View.VISIBLE
            }
        }


        binding?.reloadButton?.setOnClickListener {
            loadPage()
        }

    }

    private fun loadPage() {
        binding?.webView?.loadUrl(safeArgs.url)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}