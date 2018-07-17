package com.fsck.k9.backends

import android.content.Context
import android.net.ConnectivityManager
import com.fsck.k9.Account
import com.fsck.k9.Preferences
import com.fsck.k9.backend.BackendFactory
import com.fsck.k9.backend.api.Backend
import com.fsck.k9.backend.imap.ImapBackend
import com.fsck.k9.mail.ServerSettings
import com.fsck.k9.mail.oauth.OAuth2TokenProvider
import com.fsck.k9.mail.power.PowerManager
import com.fsck.k9.mail.ssl.DefaultTrustedSocketFactory
import com.fsck.k9.mail.store.imap.ImapStore
import com.fsck.k9.mail.transport.smtp.SmtpTransport
import com.fsck.k9.mail.transport.smtp.SmtpTransportUriCreator
import com.fsck.k9.mail.transport.smtp.SmtpTransportUriDecoder
import com.fsck.k9.mailstore.K9BackendStorage

class ImapBackendFactory(
        private val context: Context,
        private val preferences: Preferences,
        private val powerManager: PowerManager
) : BackendFactory {
    override val transportUriPrefix = "smtp"

    override fun createBackend(account: Account): Backend {
        val accountName = account.description
        val backendStorage = K9BackendStorage(preferences, account, account.localStore)
        val imapStore = createImapStore(account)
        val smtpTransport = createSmtpTransport(account)
        return ImapBackend(accountName, backendStorage, imapStore, powerManager, smtpTransport)
    }

    private fun createImapStore(account: Account): ImapStore {
        val oAuth2TokenProvider: OAuth2TokenProvider? = null
        return ImapStore(
                account,
                DefaultTrustedSocketFactory(context),
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager,
                oAuth2TokenProvider
        )
    }

    private fun createSmtpTransport(account: Account): SmtpTransport {
        val oauth2TokenProvider: OAuth2TokenProvider? = null
        return SmtpTransport(account, DefaultTrustedSocketFactory(context), oauth2TokenProvider)
    }

    override fun decodeStoreUri(storeUri: String): ServerSettings {
        return ImapStore.decodeUri(storeUri)
    }

    override fun createStoreUri(serverSettings: ServerSettings): String {
        return ImapStore.createUri(serverSettings)
    }

    override fun decodeTransportUri(transportUri: String): ServerSettings {
        return SmtpTransportUriDecoder.decodeSmtpUri(transportUri)
    }

    override fun createTransportUri(serverSettings: ServerSettings): String {
        return SmtpTransportUriCreator.createSmtpUri(serverSettings)
    }
}