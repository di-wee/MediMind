package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medimind.databinding.FragmentWebviewBinding
import android.util.Log
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar

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

        binding.webview.webViewClient = WebViewClient()
        binding.webview.settings.javaScriptEnabled = true

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

//        binding.btnBackWebView.setOnClickListener {
//            findNavController().popBackStack()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
