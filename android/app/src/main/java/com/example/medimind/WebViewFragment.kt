package com.example.medimind

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.http.SslError
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medimind.databinding.FragmentWebviewBinding
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class WebViewFragment : Fragment() {

    private var _binding: FragmentWebviewBinding? = null
    private val binding get() = _binding!!

    private var url: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled") // We harden the WebView below.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back arrow
        view.findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Use literal string "EXTERNAL_URL" here to get the URL argument
        url = arguments?.getString("EXTERNAL_URL")

        Log.d("WebViewFragment", "Received URL: $url")
        Toast.makeText(requireContext(), "Loading: $url", Toast.LENGTH_SHORT).show()

        // --- WebView hardening starts ---
        val webView = binding.webview
        val settings = webView.settings

        // Keep JS if needed but harden everything else.
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.setSupportMultipleWindows(false)

        // Block local file/content access
        settings.setAllowFileAccess(false)
        settings.setAllowContentAccess(false)
        // Extra hardening (applies even if someone tries to load file://)
        settings.allowFileAccessFromFileURLs = false
        settings.allowUniversalAccessFromFileURLs = false

        // No mixed content on HTTPS pages
        if (Build.VERSION.SDK_INT >= 21) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        // Safe Browsing (API 26+)
        if (Build.VERSION.SDK_INT >= 26) {
            settings.safeBrowsingEnabled = true
        }

        // Restrict in-WebView navigation to Wikipedia only; open others externally.
        // This now matches *.wikipedia.org (e.g., m.wikipedia.org, en.m.wikipedia.org) PLUS wikipedia.org.
        fun isWikipediaHost(host: String?): Boolean {
            if (host.isNullOrBlank()) return false
            val h = host.lowercase(Locale.US)
            return h == "wikipedia.org" || h.endsWith(".wikipedia.org")
        }

        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val target = request?.url ?: return true
                val host = target.host

                val isHttps = target.scheme.equals("https", ignoreCase = true)
                return if (isHttps && isWikipediaHost(host)) {
                    // Load inside the WebView (Wikipedia stays in-app)
                    false
                } else {
                    // Everything else goes to an external browser
                    startActivity(Intent(Intent.ACTION_VIEW, target))
                    true
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                // Never proceed on SSL errors
                handler?.cancel()
            }
        }
        // --- WebView hardening ends ---

        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
                binding.progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
                binding.progressBar.progress = newProgress
            }
        }

        binding.progressBar.max = 100

        if (!url.isNullOrBlank()) {
            binding.webview.loadUrl(url!!)
        } else {
            binding.webview.loadData("No URL provided", "text/html", "UTF-8")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
