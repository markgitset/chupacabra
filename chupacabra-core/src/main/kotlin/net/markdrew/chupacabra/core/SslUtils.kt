package net.markdrew.chupacabra.core

import java.io.File
import java.security.KeyStore
import java.security.cert.CertificateException
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Suppress("unused")
object SslUtils {

    const val TRUST_STORE_SYSPROP = "javax.net.ssl.trustStore"

    const val KEY_STORE_SYSPROP = "javax.net.ssl.keyStore"
    const val KEY_STORE_PASSWORD_SYSPROP = "javax.net.ssl.keyStorePassword"

    fun initKeyManagers(keyStore: KeyStore? = null, keyStorePassword: CharArray? = null): Array<out KeyManager>? =
        keyStore?.let { ks ->
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                init(ks, keyStorePassword)
            }.keyManagers
        }
    
    fun initSslContext(
        keyStore: KeyStore? = null, 
        keyStorePassword: CharArray? = null, 
        trustStore: KeyStore? = null
    ): SSLContext = keyStore?.let {
        SSLContext.getInstance("TLSv1.2").apply {
            //        SSLContext.getInstance("TLS").apply { 
            val keyManagers = initKeyManagers(keyStore, keyStorePassword)
            val trustManagers: Array<X509TrustManager?> = arrayOf(initX509TrustManager(trustStore))
            init(keyManagers, trustManagers, null)
        }
    } ?: SSLContext.getDefault()

    fun defaultSslSocketFactory(): SSLSocketFactory = SSLContext.getDefault().socketFactory

    /**
     * If no [trustStore] is given, the returned [X509TrustManager] will use the following system properties to configure itself:
     *
     *   javax.net.ssl.trustStore
     *   javax.net.ssl.trustStorePassword (defaults to 'changeme')
     *   javax.net.ssl.trustStoreType
     *
     * If the first of these properties is not set and no [trustStore] is given, the returned [X509TrustManager] won't trust any
     * certificates.
     */
    @JvmOverloads
    fun initX509TrustManager(trustStore: KeyStore? = null): X509TrustManager {
        val algorithm = TrustManagerFactory.getDefaultAlgorithm()
        val trustManagerFactory = TrustManagerFactory.getInstance(algorithm).apply { init(trustStore) }
        val trustManagers: List<TrustManager> = trustManagerFactory.trustManagers.asList()
        return trustManagers.firstOrNull { it is X509TrustManager } as X509TrustManager?
                ?: throw IllegalStateException("Unexpected default trust managers: $trustManagers")
    }

    /**
     * Illustrates how to check that a certificate is signed by a trusted certificate authority.
     *
     * @param args 0) path to keystore containing certificate to check 1) path to truststore
     */
    @Throws(Exception::class)
    @JvmStatic fun main(args: Array<String>) {

        // if a keystore was given as an argument, load it from disk (prompts user for keystore's password)
        val keyStoreToCheck = CertificateUtils.promptAndReadKeyStore(File(args[0]), "key store", CertificateUtils.JKS)

        val certChainToCheck = CertificateUtils.firstCertChainFrom(keyStoreToCheck)

        println()
        println("Checking certificate that claims to be")
        println("   Subject: " + certChainToCheck!![0].subjectX500Principal)
        println("   Issuer: " + certChainToCheck[0].issuerX500Principal)

        val trustStore = CertificateUtils.promptAndReadKeyStore(File(args[1]), "trust store", CertificateUtils.JKS)
        val x509TrustManager = initX509TrustManager(trustStore)

        try {
            x509TrustManager.checkClientTrusted(certChainToCheck, CertificateUtils.RSA)

            println()
            println("Success!")

        } catch (e: CertificateException) {
            println()
            println("Failed!")
            println("   " + e.message)
            //e.printStackTrace();
        }

    }

    /**
     * Configures the trust store used by the system.
     *
     * @param trustStore
     * @param trustStorePassword
     * @param trustStoreType
     */
    fun configureSystemTrustStore(trustStore: String, trustStorePassword: String, trustStoreType: String?) {
        setNullableSystemProperty(TRUST_STORE_SYSPROP, trustStore)
        setNullableSystemProperty("javax.net.ssl.trustStorePassword", trustStorePassword)
        setNullableSystemProperty("javax.net.ssl.trustStoreType", trustStoreType)
    }

    /**
     * Sets system property, clearing the property if the given value is null.
     */
    fun setNullableSystemProperty(property: String, value: String?) {
        value?.let { System.setProperty(property, value) } ?: System.clearProperty(property)
    }

    /**
     * Configures the key store used by the system. For example, this keystore will be used for client connections to secure web
     * services.
     *
     * @param keyStore
     * @param keyStorePassword
     * @param keyStoreType
     */
    fun configureSystemSsl(keyStore: String, keyStorePassword: String, keyStoreType: String?) {
        setNullableSystemProperty(KEY_STORE_SYSPROP, keyStore)
        setNullableSystemProperty(KEY_STORE_PASSWORD_SYSPROP, keyStorePassword)
        setNullableSystemProperty("javax.net.ssl.keyStoreType", keyStoreType)
    }

}
