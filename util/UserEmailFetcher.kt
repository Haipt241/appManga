package com.bigberry.comicvn.util

/**
 * Created by ThangBK on 7/20/17.
 */
import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

/**
 * This class uses the AccountManager to get the primary email address of the
 * current user.
 */
object UserEmailFetcher {

    fun getEmail(context: Context): String {
        val accountManager = AccountManager.get(context)
        val account = getAccount(accountManager)

        if (account == null) {
            return "no"
        } else {
            return account.name
        }
    }

    private fun getAccount(accountManager: AccountManager): Account? {
        val accounts = accountManager.getAccountsByType("com.google")
        val account: Account?
        if (accounts.isNotEmpty()) {
            account = accounts[0]
        } else {
            account = null
        }
        return account
    }
}