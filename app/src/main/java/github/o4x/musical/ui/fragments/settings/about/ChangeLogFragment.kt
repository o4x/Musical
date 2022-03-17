package github.o4x.musical.ui.fragments.settings.about

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.o4x.appthemehelper.util.ATHUtil
import github.o4x.musical.R
import github.o4x.musical.databinding.FragmentWebViewBinding
import github.o4x.musical.prefs.PreferenceUtil
import java.io.BufferedReader
import java.io.InputStreamReader

class ChangeLogFragment : Fragment(R.layout.fragment_web_view) {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = binding.webView
        try {
            // Load from musical-changelog.html in the assets folder
            val buf = StringBuilder()
            val json = requireActivity().assets.open("musical-changelog.html")
            val `in` = BufferedReader(InputStreamReader(json, "UTF-8"))
            var str: String?
            while (`in`.readLine().also { str = it } != null) buf.append(str)
            `in`.close()

            // Inject color values for WebView body background and links
            val backgroundColor = colorToCSS(
                ATHUtil.resolveColor(
                    requireActivity(),
                    R.attr.backgroundColor,
                    Color.parseColor(if (PreferenceUtil.isDarkMode) "#424242" else "#ffffff")
                )
            )
            val contentColor =
                colorToCSS(Color.parseColor(if (PreferenceUtil.isDarkMode) "#ffffff" else "#000000"))
            val changeLog = buf.toString()
                .replace(
                    "{style-placeholder}",
                    String.format(
                        "body { background-color: %s; color: %s; }",
                        backgroundColor,
                        contentColor
                    )
                )
//                .replace(
//                    "{link-color}",
//                    colorToCSS(ThemeSingleton.get().positiveColor.getDefaultColor())
//                )
//                .replace(
//                    "{link-color-active}",
//                    colorToCSS(lightenColor(ThemeSingleton.get().positiveColor.getDefaultColor()))
//                )
            webView.loadData(changeLog, "text/html", "UTF-8")
        } catch (e: Throwable) {
            webView.loadData(
                "<h1>Unable to load</h1><p>" + e.localizedMessage + "</p>",
                "text/html",
                "UTF-8"
            )
        }
    }

    companion object {

        fun setChangelogRead(context: Context) {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersion = pInfo.versionCode
                PreferenceUtil.setLastChangeLogVersion(currentVersion)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        private fun colorToCSS(color: Int): String {
            return String.format(
                "rgb(%d, %d, %d)",
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            ) // on API 29, WebView doesn't load with hex colors
        }
    }
}