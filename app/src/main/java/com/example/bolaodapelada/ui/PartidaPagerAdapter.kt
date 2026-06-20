package com.example.bolaodapelada.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PartidaPagerAdapter(
    activity: FragmentActivity,
    private val usuarioId: Long
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListaPartidasFragment.newInstance(usuarioId, "FUTURA")
            else -> ListaPartidasFragment.newInstance(usuarioId, "PASSADA")
        }
    }
}