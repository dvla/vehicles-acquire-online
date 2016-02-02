package email

import java.text.SimpleDateFormat
import org.joda.time.DateTime
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
                                     transactionTimestamp: DateTime): Contents = {
    Contents(
      buildHtml(vehicleDetails, imagesPath, buildNewKeeperHtml(vehicleDetails, transactionId, imagesPath)),
      buildText(buildNewKeeperText(vehicleDetails, transactionId))
    )
  }

  def buildTraderConfirmationWith(vehicleDetails: VehicleAndKeeperDetailsModel,
                                  transactionId: String, imagesPath: String,
                                  transactionTimestamp: DateTime): Contents = {
    val transactionTimestampStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(transactionTimestamp.toDate)

    Contents(
      buildHtml(vehicleDetails, imagesPath, buildTraderHtml(vehicleDetails, transactionId, transactionTimestampStr)),
      buildText(buildTraderText(vehicleDetails, transactionId, transactionTimestampStr))
    )
  }

  private def buildHtml(vehicleDetails: VehicleAndKeeperDetailsModel,
                        imagesPath: String,
                        htmlContent: String): String =

    s"""
       |
       |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
       |<html xmlns="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml">
       |<head>
       |    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
       |    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
       |    <title>${vehicleDetails.registrationNumber} Confirmation of new vehicle keeper</title>
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
       |                            <p><strong style="text-decoration: underline">This is an automated email - Please do not reply as emails received at this address cannot be responded to.</strong></p>
       |$htmlContent
       |
       |                            <p>For more information on driving and transport go to <a href="http://www.gov.uk/browse/driving" target="_blank">www.gov.uk/browse/driving</a>.</p>
       |
       |                            <p>You may wish to save or print this email confirmation for your records.</p>
       |
       |                            <p>Yours sincerely <br />
       |                            Rohan Gye<br />
       |                            Vehicles Service Manager
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

  private def buildNewKeeperHtml(vehicleDetails: VehicleAndKeeperDetailsModel,
                                 transactionId: String, imagesPath: String): String =
    s"""
       |                            <p>DVLA have been notified electronically that you are now the new keeper of Vehicle Registration Number: <strong>${vehicleDetails.registrationNumber}</strong></p>
       |
       |                            <p>The online Transaction ID is <strong>$transactionId</strong></p>
       |
       |                            <p>You should receive your new V5C (log book) within 2 weeks.</p>
       |
       |                            <p>Since 1st October 2014, vehicle tax can no longer be transferred as part of the sale. This is because the seller will automatically receive a refund of any remaining tax.</p>
       |
       |                            <p>You must tax this vehicle before it is driven on the road, tax now at <a href="http://www.gov.uk/vehicletax" target="_blank">www.gov.uk/vehicletax</a>.</p>
       |
       |                            <p>If you do not want to tax you can make a SORN declaration now at <a href="http://www.gov.uk/sorn" target="_blank">www.gov.uk/sorn</a>.</p>
       |
    """.stripMargin

  private def buildTraderHtml(vehicleDetails: VehicleAndKeeperDetailsModel,
                              transactionId: String,
                              transactionTimestamp: String): String =

    s"""
       |                            <p>Thank you for using DVLA’s online service to confirm you have sold this vehicle out of the motor trade. Please destroy the original V5C as this must not be sent to DVLA. The V5C/2 (green slip) should have been passed to the new keeper.</p>
       |
       |                            <p>The application details are:</p>
       |
       |                            <p>
       |                            Vehicle registration number: <strong>${vehicleDetails.registrationNumber}</strong><br>
       |                            Transaction ID: <strong>$transactionId</strong><br>
       |                            Application made on: <strong>$transactionTimestamp</strong>
       |                            </p>
       |
       |                            <p>The new keeper should receive their new V5C within 2 weeks.</p>
       |
       |                            <p>As vehicle tax or SORN can no longer be transferred as part of the sale, the new keeper must tax this vehicle before it is driven on the road at <a href="http://www.gov.uk/vehicletax" target="_blank">www.gov.uk/vehicletax</a>. They can make a SORN at <a href="http://www.gov.uk/sorn" target="_blank">www.gov.uk/sorn</a>.</p>
       |
    """.stripMargin

  private def buildText(content: String): String =
    s"""
       |THIS IS AN AUTOMATED EMAIL - Please do not reply as emails received at this address cannot be responded to.
       |$content
       |For more information on driving and transport go to http://www.gov.uk/browse/driving
       |
       |You may wish to save or print this email confirmation for your records.
       |
       | Yours sincerely
       | Rohan Gye
       | Vehicles Service Manager
    """.stripMargin

    private def buildNewKeeperText(vehicleDetails: VehicleAndKeeperDetailsModel,
                                   transactionId: String): String =
      s"""
         |DVLA have been notified electronically that you are now the new keeper of Vehicle Registration Number: ${vehicleDetails.registrationNumber}
         |
         |The online Transaction ID is $transactionId
         |
         |You should receive your new V5C (log book) within 2 weeks.
         |
         |Since 1st October 2014, vehicle tax can no longer be transferred as part of the sale. This is because the seller will automatically receive a refund of any remaining tax.
         |
         |You must tax this vehicle before it is driven on the road, tax now at http://www.gov.uk/vehicletax
         |
         |If you do not want to tax you can make a SORN declaration now at http://www.gov.uk/sorn
      """.stripMargin

   private def buildTraderText(vehicleDetails: VehicleAndKeeperDetailsModel,
                               transactionId: String,
                               transactionTimestamp: String): String =
      s"""
         |Thank you for using DVLA's online service to confirm you have sold this vehicle out of the motor trade. Please destroy the original V5C as this must not be sent to DVLA. The V5C/2 (green slip) should have been passed to the new keeper.
         |
         |The application details are:
         |
         |Vehicle Registration Number: ${vehicleDetails.registrationNumber}
         |Transaction ID is $transactionId
         |Application made on: $transactionTimestamp
         |
         |The new keeper should receive their new V5C within 2 weeks.
         |
         |As vehicle tax or SORN can no longer be transferred as part of the sale, the new keeper must tax this vehicle before it is driven on the road at http://www.gov.uk/vehicletax. They can make a SORN at http://www.gov.uk/sorn.
      """.stripMargin
}
