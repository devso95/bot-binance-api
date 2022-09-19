package deso.future_bot.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import deso.future_bot.bot.data.Gender
import deso.future_bot.util.Constants
import deso.future_bot.util.PhoneUtil
import org.apache.commons.lang3.StringUtils
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Entity
@Table(name = "xx_user")
class User : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userGenerator")
    @SequenceGenerator(name = "userGenerator", allocationSize = 1)
    var id: Long? = null

    // Ten dinh danh tren toan he thong
    @Column(length = 50, unique = true, nullable = false)
    private var login: @NotNull @Pattern(regexp = Constants.LOGIN_REGEX) @Size(min = 1, max = 50) String? = null

    @JsonIgnore
    @Column(name = "password_hash", length = 60, nullable = false)
    var password: @NotNull @Size(min = 60, max = 60) String? = null

    @Column(name = "first_name", length = 50)
    var firstName: @Size(max = 50) String? = null

    @Column(name = "last_name", length = 50)
    var lastName: @Size(max = 50) String? = null

    @Column(length = 254)
    var email: @Email @Size(max = 254) String? = null

    @Column(nullable = false)
    var activated = true

    @Column(name = "image_url", length = 256)
    var imageUrl: @Size(max = 256) String? = null

    /**
     * Số điện thoại liên hệ
     */
    @Column(name = "phone_number", length = 20, nullable = false)
    var phoneNumber: @NotNull @Size(max = 20) @Pattern(regexp = PhoneUtil.PHONE_REGEX) String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    var gender: Gender? = null

    @Column(name = "birth_day")
    var birthDay: LocalDate? = null

    @Column(name = "created_date", updatable = false)
    @JsonIgnore
    private val createdDate = Instant.now()

    fun getLogin(): String? {
        return login
    }

    // Lowercase the login before saving it in database
    fun setLogin(login: String?) {
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        return if (o !is User) {
            false
        } else id != null && id == o.id
    }

    override fun hashCode(): Int {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return javaClass.hashCode()
    }

    // prettier-ignore
    override fun toString(): String {
        return "User{" +
                "login='" + login + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", activated='" + activated + '\'' +
                "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}