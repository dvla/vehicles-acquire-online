package email

import java.text.SimpleDateFormat
import org.joda.time.DateTime
import play.api.i18n.{Lang, Messages}
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

/**
 * The email message builder class will create the contents of the message. override the buildHtml and buildText
 * with new html and text templates respectively.
 *
 */
object EmailMessageBuilder {
  import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents

  def buildNewKeeperConfirmationWith(vehicleDetails: VehicleAndKeeperDetailsModel,
                                     transactionId: String, imagesPath: String,
                                     transactionTimestamp: DateTime)(implicit lang: Lang): Contents = {
    Contents(
      buildHtml(vehicleDetails.registrationNumber, imagesPath,
        buildNewKeeperHtml(vehicleDetails.registrationNumber, transactionId)),
      buildText(buildNewKeeperText(vehicleDetails.registrationNumber, transactionId))
    )
  }

  def buildTraderConfirmationWith(vehicleDetails: VehicleAndKeeperDetailsModel,
                                  transactionId: String, imagesPath: String,
                                  transactionTimestamp: DateTime)(implicit lang: Lang): Contents = {
    val transactionTimestampStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(transactionTimestamp.toDate)

    Contents(
      buildHtml(vehicleDetails.registrationNumber, imagesPath,
        buildTraderHtml(vehicleDetails.registrationNumber, transactionId, transactionTimestampStr)),
      buildText(buildTraderText(vehicleDetails.registrationNumber, transactionId, transactionTimestampStr))
    )
  }

  private def buildHtml(regNumber: String,
                        imagesPath: String,
                        htmlContent: String)(implicit lang: Lang): String =

    s"""
       |
       |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
       |<html xmlns="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml">
       |<head>
       |    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
       |    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
       |    <title>${regNumber} ${Messages("email.title.keeper")}</title>
       |</head>
       |
       |<body style="width: 100% !important; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; margin: 0; padding: 0;">
       |    <style type="text/css">
       |    p {
       |        color: #000000;
       |        font-size: 19px;
       |        line-height: 22px;
       |        font-family: Helvetica, Arial, sans, serif;
       |    }
       |    a {
       |        color: #2e3191;
       |        font-size: 19px;
       |        text-decoration: underline;
       |    }
       |    strong {
       |        font-weight: bold;
       |    }
       |    </style>
       |
       |    <table cellpadding="0" cellspacing="0" border="0" style="width: 100% !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-family: Helvetica, Arial, sans, sans-serif; background: #fff; margin: 0; padding: 0;" bgcolor="#fff">
       |        <tr>
       |            <td id="GovUkContainer" style="border-collapse: collapse; color: #fff; background: #000; padding: 0 30px;" bgcolor="#000">
       |                <table style="border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
       |                    <tr>
       |                        <td style="border-collapse: collapse; padding: 20px 0;">
       |                            <a target="_blank" href="https://www.gov.uk/" style="color: #ffffff; text-decoration: none;">
       |                                <img src="${imagesPath}/gov-uk.jpg" width="320" height="106" alt="Crown image" style="outline: none; text-decoration: none; -ms-interpolation-mode: bicubic;" />
       |                            </a>
       |                        </td>
       |                    </tr>
       |                </table>
       |            </td>
       |        </tr>
       |
       |        <tr>
       |            <td valign="top" style="border-collapse: collapse; padding: 0 30px;">
       |
       |                <table cellpadding="0" cellspacing="0" border="0" width="100%" style="border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;">
       |
       |                    <tr>
       |                        <td style="border-collapse: collapse;">
       |
       |                            <p><strong style="text-decoration: underline">${Messages("email.template.line1")}</strong></p>
       |$htmlContent
       |
       |                            <p>${Messages("email.template.line2Html")}</p>
       |
       |                            <p>${Messages("email.template.line3")}</p>
       |
       |                            <p>${Messages("email.signature.p1")}<br>
       |                            ${Messages("email.signature.p2")}<br>
       |                            ${Messages("email.signature.p3")}
       |                            </p>
       |
       |                </table>
       |            </td>
       |        </tr>
       |    </table>
       |    <!-- End of wrapper table -->
       |</body>
       |
       |</html>
    """.stripMargin

  private def buildNewKeeperHtml(regNumber: String,
                                 transactionId: String)(implicit lang: Lang): String =
    s"""
       |                            <p>${Messages("email.newKeeper.p1")} <strong>${regNumber}</strong></p>
       |
       |                            <p>${Messages("email.newKeeper.p2")} <strong>${transactionId}</strong></p>
       |
       |                            <p>${Messages("email.newKeeper.p3")}</p>
       |
       |                            <p>${Messages("email.newKeeper.p4")}</p>
       |
       |                            <p>${Messages("email.newKeeper.p5Html")}</p>
       |
       |                            <p>${Messages("email.newKeeper.p6Html")}</p>
       |
    """.stripMargin

  private def buildTraderHtml(regNumber: String,
                              transactionId: String,
                              transactionTimestamp: String)(implicit lang: Lang): String =
    s"""
       |                            <p>${Messages("email.trader.p1")}</p>
       |
       |                            <p>${Messages("email.txndetails.p1")}</p>
       |
       |                            <p>${Messages("email.txndetails.p2")} <strong>${regNumber}</strong>
       |                            <br>${Messages("email.txndetails.p3")} <strong>${transactionId}</strong>
       |                            <br>${Messages("email.txndetails.p4")} <strong>${transactionTimestamp}</strong></p>
       |
       |                            <p>${Messages("email.trader.p2")}</p>
       |
       |                            <p>${Messages("email.trader.p3")}</p>
       |
    """.stripMargin

  private def buildText(content: String)(implicit lang: Lang): String =
    s"""
       |${Messages("email.template.line1")}
       |$content
       |${Messages("email.template.line2")}
       |
       |${Messages("email.template.line3")}
       |
       |${Messages("email.signature.p1")}
       |${Messages("email.signature.p2")}
       |${Messages("email.signature.p3")}
    """.stripMargin

    private def buildNewKeeperText(regNumber: String,
                                   transactionId: String)(implicit lang: Lang): String =
      s"""
         |${Messages("email.newKeeper.p1")} ${regNumber}
         |
         |${Messages("email.newKeeper.p2")} ${transactionId}
         |
         |${Messages("email.newKeeper.p3")}
         |
         |${Messages("email.newKeeper.p4")}
         |
         |${Messages("email.newKeeper.p5")}
         |
         |${Messages("email.newKeeper.p6")}
      """.stripMargin

   private def buildTraderText(regNumber: String,
                               transactionId: String,
                               transactionTimestamp: String)(implicit lang: Lang): String =
      s"""
         |${Messages("email.trader.p1")}
         |
         |${Messages("email.txndetails.p1")}
         |
         |${Messages("email.txndetails.p2")} ${regNumber}
         |${Messages("email.txndetails.p3")} ${transactionId}
         |${Messages("email.txndetails.p4")} ${transactionTimestamp}
         |
         |${Messages("email.trader.p2")}
         |
         |${Messages("email.trader.p3")}""".stripMargin
}
