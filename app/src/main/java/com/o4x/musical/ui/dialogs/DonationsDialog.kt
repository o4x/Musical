package com.o4x.musical.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.ThemeStore.Companion.textColorPrimary
import code.name.monkey.appthemehelper.ThemeStore.Companion.textColorSecondary
import code.name.monkey.appthemehelper.util.ATHUtil.resolveColor
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import com.o4x.musical.R
import java.lang.ref.WeakReference
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class DonationsDialog : DialogFragment(), IBillingHandler {
    private val billingProcessor: BillingProcessor? = null
    private var skuDetailsLoadAsyncTask: AsyncTask<*, *, *>? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        billingProcessor = new BillingProcessor(getContext(), App.GOOGLE_PLAY_LICENSE_KEY, this);
        @SuppressLint("InflateParams") val customView = LayoutInflater.from(
            context
        ).inflate(R.layout.dialog_donation, null)
        val progressBar = customView.findViewById<ProgressBar>(R.id.progress)
        //        MDTintHelper.setTint(progressBar, ThemeSingleton.get().positiveColor.getDefaultColor());
        return MaterialDialog(requireContext())
            .title(R.string.support_development)
            .customView(view = customView)
    }

    private fun donate(i: Int) {
        val ids = resources.getStringArray(DONATION_PRODUCT_IDS)
        billingProcessor!!.purchase(activity, ids[i])
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        loadSkuDetails()
        Toast.makeText(context, R.string.thank_you, Toast.LENGTH_SHORT).show()
    }

    override fun onPurchaseHistoryRestored() {
        loadSkuDetails()
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.e(TAG, "Billing error: code = $errorCode", error)
    }

    override fun onBillingInitialized() {
        loadSkuDetails()
    }

    override fun onDestroy() {
        billingProcessor?.release()
        if (skuDetailsLoadAsyncTask != null) {
            skuDetailsLoadAsyncTask!!.cancel(true)
        }
        super.onDestroy()
    }

    private fun loadSkuDetails() {
        if (skuDetailsLoadAsyncTask != null) {
            skuDetailsLoadAsyncTask!!.cancel(false)
        }
        skuDetailsLoadAsyncTask = SkuDetailsLoadAsyncTask(this).execute()
    }

    private class SkuDetailsLoadAsyncTask(donationsDialog: DonationsDialog) :
        AsyncTask<Void?, Void?, List<SkuDetails>?>() {
        private val donationDialogWeakReference: WeakReference<DonationsDialog>
        override fun onPreExecute() {
            super.onPreExecute()
            val dialog = donationDialogWeakReference.get() ?: return
            val customView: View = (dialog.dialog as MaterialDialog?)!!.getCustomView()
            customView.findViewById<View>(R.id.progress_container).visibility =
                View.VISIBLE
            customView.findViewById<View>(R.id.list).visibility = View.GONE
        }

        override fun doInBackground(vararg params: Void?): List<SkuDetails>? {
            val dialog = donationDialogWeakReference.get()
            if (dialog != null) {
                val ids = dialog.resources.getStringArray(DONATION_PRODUCT_IDS)
                return dialog.billingProcessor!!.getPurchaseListingDetails(ArrayList(Arrays.asList(*ids)))
            }
            cancel(false)
            return null
        }

        override fun onPostExecute(skuDetails: List<SkuDetails>?) {
            super.onPostExecute(skuDetails)
            val dialog = donationDialogWeakReference.get() ?: return
            if (skuDetails == null || skuDetails.isEmpty()) {
                dialog.dismiss()
                return
            }
            val customView: View = (dialog.dialog as MaterialDialog?)!!.getCustomView()
            customView.findViewById<View>(R.id.progress_container).visibility = View.GONE
            val listView = customView.findViewById<ListView>(R.id.list)
            listView.adapter =
                SkuDetailsAdapter(dialog, skuDetails)
            listView.visibility = View.VISIBLE
        }

        init {
            donationDialogWeakReference = WeakReference(donationsDialog)
        }
    }

    internal class SkuDetailsAdapter(
        var donationsDialog: DonationsDialog,
        objects: List<SkuDetails>
    ) : ArrayAdapter<SkuDetails?>(
        donationsDialog.requireContext(), LAYOUT_RES_ID, objects
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(LAYOUT_RES_ID, parent, false)
            }
            val skuDetails = getItem(position)
            val viewHolder = ViewHolder(convertView)
            viewHolder.title!!.text =
                skuDetails!!.title.replace("(Musical Music Player)", "").trim { it <= ' ' }
            viewHolder.text!!.text = skuDetails.description
            viewHolder.price!!.text = skuDetails.priceText
            val purchased = donationsDialog.billingProcessor!!.isPurchased(
                skuDetails.productId
            )
            val titleTextColor = if (purchased) resolveColor(
                context,
                android.R.attr.textColorHint
            ) else textColorPrimary(
                context
            )
            val contentTextColor = if (purchased) titleTextColor else textColorSecondary(
                context
            )
            viewHolder.title!!.setTextColor(titleTextColor)
            viewHolder.text!!.setTextColor(contentTextColor)
            viewHolder.price!!.setTextColor(titleTextColor)
            strikeThrough(viewHolder.title, purchased)
            strikeThrough(viewHolder.text, purchased)
            strikeThrough(viewHolder.price, purchased)
            convertView!!.setOnTouchListener { v: View?, event: MotionEvent? -> purchased }
            convertView.setOnClickListener { v: View? -> donationsDialog.donate(position) }
            return convertView
        }

        internal class ViewHolder(view: View?) {
            @JvmField
            @BindView(R.id.album_name)
            var title: TextView? = null

            @JvmField
            @BindView(R.id.text)
            var text: TextView? = null

            @JvmField
            @BindView(R.id.price)
            var price: TextView? = null

            init {
                ButterKnife.bind(this, view!!)
            }
        }

        companion object {
            @LayoutRes
            private val LAYOUT_RES_ID = R.layout.item_donation_option
            private fun strikeThrough(textView: TextView?, strikeThrough: Boolean) {
                textView!!.paintFlags =
                    if (strikeThrough) textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    companion object {
        val TAG = DonationsDialog::class.java.simpleName
        private const val DONATION_PRODUCT_IDS = R.array.donation_ids
        fun create(): DonationsDialog {
            return DonationsDialog()
        }
    }
}