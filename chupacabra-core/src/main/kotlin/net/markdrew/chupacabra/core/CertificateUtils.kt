package net.markdrew.chupacabra.core

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.naming.ldap.LdapName

@Suppress("unused")
object CertificateUtils {

    // standard key store types
    const val PKCS12 = "pkcs12"
    const val JKS = "jks"

    // standard authentication types
    const val RSA = "RSA"
    const val DHE_DSS = "DHE_DSS"

    /**
     * Reads a key store, prompting the user for the necessary password.
     *
     * @param storeSource
     * @param keyStoreType
     * @param storeDesc
     * @return
     * @throws KeyStoreException
     */
    @Throws(KeyStoreException::class)
    fun promptAndReadKeyStore(storeSource: InputStream, storeDesc: String = "PKI", keyStoreType: String = PKCS12): KeyStore =
        PasswordPrompter(storeDesc).useAndClearPassword { password -> readKeyStore(storeSource, password, keyStoreType) }

    @Throws(KeyStoreException::class)
    fun promptAndReadKeyStore(storeFile: File, storeDesc: String = "PKI", keyStoreType: String = PKCS12): KeyStore =
        storeFile.inputStream().use { promptAndReadKeyStore(it, storeDesc, keyStoreType) }

    /**
     * Reads a key store, given its password.
     *
     * @param storeSource
     * @param keyStoreType
     * @param password
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class, CertificateException::class, IOException::class)
    fun readKeyStore(storeSource: InputStream, password: CharArray, keyStoreType: String = PKCS12): KeyStore =
        KeyStore.getInstance(keyStoreType).apply { load(storeSource, password) }

    /**
     * Reads a key store, given its password.
     *
     * @param storeFile
     * @param keyStoreType
     * @param password
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class, CertificateException::class, IOException::class)
    fun readKeyStore(storeFile: File, password: CharArray, keyStoreType: String = PKCS12): KeyStore =
        storeFile.inputStream().use { readKeyStore(it, password, keyStoreType) }

    /**
     * @param keyStore
     * @return the first non-null certificate in the given store
     * @throws KeyStoreException
     */
    @Throws(KeyStoreException::class)
    fun firstCertFrom(keyStore: KeyStore): X509Certificate? {
        val firstCertChain: Array<X509Certificate>? = firstCertChainFrom(keyStore)
        return if (firstCertChain == null || firstCertChain.isEmpty()) null else firstCertChain[0]
    }

    /**
     * @param keyStore
     * @return the first non-null certificate chain in the given store
     * @throws KeyStoreException
     */
    @Throws(KeyStoreException::class)
    fun firstCertChainFrom(keyStore: KeyStore): Array<X509Certificate>? {

        val aliases = keyStore.aliases()
        if (!aliases.hasMoreElements()) return null

        val certificates: Array<out Certificate> = keyStore.getCertificateChain(aliases.nextElement()) ?: return null

        // "cast" the result to an array of X509Certificates
        @Suppress("UNCHECKED_CAST")
        val x509List: List<X509Certificate> = certificates.asList() as List<X509Certificate>
        return x509List.toTypedArray()
    }

    /**
     * For example, given "CN=Fred Flintstone,OU=Shipping Dept,O=Acme Rock,C=US", returns "Fred Flintstone"
     *
     * @param dnString the distinguished name (DN) from which to extract the common name (CN)
     * @return the extracted common name (CN)
     */
    fun parseCnFromDn(dnString: String): String {
        val rdnStream = LdapName(dnString).rdns.stream()
        val cnRdn = rdnStream.filter { rdn -> "cn".equals(rdn.type, ignoreCase = true) }.findFirst()
        return cnRdn.map { it.getValue() }.map { String::class.java.cast(it) }
            .orElseThrow { IllegalArgumentException("No cn was found in the dn: $dnString") }
    }
}
