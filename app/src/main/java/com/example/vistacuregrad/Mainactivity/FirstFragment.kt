package com.example.vistacuregrad.Mainactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.ImageViewTarget
import com.example.vistacuregrad.R

class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)

        val imageView: ImageView = view.findViewById(R.id.imageView)

        Glide.with(this)
            .asGif()
            .load(R.drawable.filledeyelogo)
            .into(object : ImageViewTarget<GifDrawable>(imageView) {
                override fun setResource(resource: GifDrawable?) {
                    resource?.let {
                        it.setLoopCount(1)
                        it.start()

                        it.registerAnimationCallback(object :
                            Animatable2Compat.AnimationCallback() {
                            override fun onAnimationEnd(drawable: android.graphics.drawable.Drawable?) {

                                findNavController().navigate(R.id.action_firstFragment_to_introFragment)
                            }
                        })
                    }
                    imageView.setImageDrawable(resource)
                }
            })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Restore default system UI visibility for other fragments
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}



