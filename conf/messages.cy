# VehicleLookup form error
error.restricted.validKeeperOption = Please select whether the vehicle is being sold to a private individual or a business
error.validDocumentReferenceNumber = Document reference number must be an 11-digit number

error.title = An unrecoverable error has occurred
error.button = Start again

# VPC - PrivateKeeperDetailsBase
error.validDriverNumber = The driver number that you have entered does not appear to be in an acceptable format. Please amend your entry or delete and re-submit.
error.titlePlusFirstName.tooLong = The combined length of your title e.g. Mr/Mrs/Miss/Other and first name(s) cannot exceed 26 characters. Please amend your entry and re-submit.
select = Tax/SORN
VehicleTaxOrSornFormModel = SORN now
error.sornformerror = You must accept the SORN option.
error.nosornformerror = you must select the SORN now option.
error.sornselectid = One of the options must be selected.

##
# ACQUIRE VEHICLE
##

acquire.backbutton = Back
acquire_vehicledetails.warning = If the vehicle details shown are incorrect please abandon this transaction and send the V5C registration certificate (logbook) with a covering letter to DVLA, Swansea, SA99 1BA.

# 404 onHandlerNotFound
onHandlerNotFound.title = This page cannot be found
onHandlerNotFound.header = This page cannot be found
onHandlerNotFound.body = We are unable to find the page you have requested, this event has been logged for investigation.
onHandlerNotFound.noJavascript = If you wish to try again select the back button on your browser, if not select "Exit" to leave the service.
onHandlerNotFound.start.button = Start

mandatory = Fields not labelled (optional) are mandatory

#page 1 - Before You Start
acquire_beforeyoustart.title = Selling a vehicle out of trade
acquire_beforeyoustart.p0 = If a vehicle has been sold from the motor trader, the motor trader selling the vehicle can now tell DVLA online.
acquire_beforeyoustart.p1 = This service must only be used by a member of the motor trade acting on behalf of a buyer.
acquire_beforeyoustart.p2 = Before you start:
acquire_beforeyoustart.p3 = To use this service you will need:
acquire_beforeyoustart.p4 = Please note: this service updates our records immediately.
acquire_beforeyoustart.p5 = Once you’ve finished:
acquire_beforeyoustart.p6 = Other ways to apply:
acquire_beforeyoustart.p7 = If you do not use the online service, you must complete the V5C registration certificate (logbook) and post it to DVLA, Swansea, SA99 1BA
acquire_beforeyoustart.listitem1 = no vehicle or tax class changes can be made online
acquire_beforeyoustart.listitem2.1 = you will not be able to use this online service to assign or transfer a vehicle registration number.
acquire_beforeyoustart.listitem2.link = Apply to retain a vehicle registration number (V317)
acquire_beforeyoustart.listitem2.2 =, you will need to send the completed application to the DVLA, Swansea, SA99 1DS.
acquire_beforeyoustart.listitem3 = this vehicle must be taxed before it can be driven on the road. You can tax or SORN your vehicle on behalf of the buyer as part of this service.
acquire_beforeyoustart.listitem4 = DVLA cannot record joint keepers
acquire_beforeyoustart.listitem5 = this service is currently available Mon-Sat, 8am – 6pm
acquire_beforeyoustart.listitem6 = the 11 digit document reference number from the latest V5C registration certificate (logbook).
acquire_beforeyoustart.listitem7 =the consent of the buyer of the vehicle to act on their behalf
acquire_beforeyoustart.listitem8 = the new keeper (buyer) should receive a new V5C registration certificate (logbook) within 5 working days.
acquire_beforeyoustart.listitem9 = the original V5C registration certificate (logbook) used to transact online must be destroyed and must not be sent to DVLA
acquire_beforeyoustart.link1 = Motor traders: tell DVLA about the sale of a vehicle
acquire_beforeyoustart.link2 = Vehicle tax changes
acquire_beforeyoustart.button = Start now

#page 2 - Setup trade details
acquire_setuptradedetails.title = Provide trader details
acquire_setuptradedetails.tradernamelabel = Trader name
acquire_setuptradedetails.tradername.hint = Name of the company that the vehicle is being sold from
acquire_setuptradedetails.traderpostcodelabel = Postcode
acquire_setuptradedetails.traderpostcode.hint = e.g. AB1 2BA or AB12BA
acquire_setuptradedetails.traderemaillabel = Trader email address
acquire_setuptradedetails.button = Find address
acquire_setuptradedetails.traderemailoption = Do you want an email confirmation of the sale?
acquire_setuptradedetails.traderemailoption.yes = Yes
acquire_setuptradedetails.traderemailoption.no = No

# gatling
traderName = Trader name
traderPostcode = Postcode
traderEmail = Trader email address
traderEmailOption = Email confirmation

#page 3 - Business Choose Your Address
disposal_businessChooseYourAddress_addressSelect = Select trader address
disposal_businessChooseYourAddress.address.required = Please select a valid address

acquire_businessChooseYourAddress_title = Select trader address
acquire_businessChooseYourAddress_businessName_label = Trader name
acquire_businessChooseYourAddress_businessName = {0}
acquire_businessChooseYourAddress_businessEmail_label = Email
acquire_businessChooseYourAddress.emailNotEntered = Not entered
acquire_businessChooseYourAddress_postcode_label = Postcode
acquire_businessChooseYourAddress_addressSelect_label = Choose your address
acquire_businessChooseYourAddress.submitbutton = Next
acquire_businessChooseYourAddress.manualAddressEntry = Address is not in the list
acquire_businessChooseYourAddress.manualAddressEntry.enterManuallyButton = Enter address
acquire_businessChooseYourAddress_noaddressesfound.h2.1 = No addresses found for that postcode.
acquire_businessChooseYourAddress_noaddressesfound.h2.2 = Please either ''Enter address'' or click ''Back'' to try again.

#page 4 - vehicle Lookup
acquire_vehiclelookup.title = Enter vehicle details
acquire_vehiclelookup.p2 = Trader details
acquire_vehiclelookup.v5c = V5C registration certificate (logbook)
acquire_vehiclelookup.refnumlabel = Latest V5C registration certificate (logbook) document reference number
acquire_vehiclelookup.vrninputlabel = Vehicle registration number
acquire_vehiclelookup.button = Next
acquire_vehiclelookup.tradernamelabel = Trader name
acquire_vehiclelookup.traderaddresslabel = Address
acquire_vehiclelookup.traderemaillabel = Email
acquire_vehiclelookup.traderresetdetails = Change these trader details?

vehicleSoldTo = Vehicle sold to
acquire_vehiclelookup.vehiclesoldto.label = Is the vehicle being sold to a private individual or a business?
acquire_vehiclelookup.vehiclesoldto.private = Private individual
acquire_vehiclelookup.vehiclesoldto.business = Business

vehicleRegistrationNumber = Vehicle registration number
documentReferenceNumber = Document reference number

#vehicle lookup failure
acquire_vehiclelookupfailure.title = Unable to find a vehicle record
acquire_vehiclelookupfailure.button = Try again
acquire_vehiclelookupfailure.button2 = Exit

vehicle_and_keeper_lookup_failure.p0 = The vehicle registration number and/or the V5C registration certificate (logbook) document reference number entered is either incorrect or does not come from the latest V5C issued for this vehicle.
vehicle_and_keeper_lookup_failure.p1 = Please check that the vehicle registration number and V5C registration certificate (logbook) document reference number are correct and select the ‘Try again’ button below.
vehicle_and_keeper_lookup_failure.p2 = If you are sure that the details have been entered correctly then you may be using an old V5C registration certificate (logbook) and it’s likely that we have issued you with a new one since.
vehicle_and_keeper_lookup_failure.p3 = If you do not have the latest issued V5C registration certificate (logbook), you will need to send your completed V5C registration certificate (logbook) to DVLA, Swansea, SA99 1BD.
vehicle_and_keeper_lookup_failure.p4 = You will only have a limited number of attempts to enter the vehicle details for this vehicle.

#page 3 [optional] - manual address entry
acquire_enteraddressmanually.title = Enter address
acquire_enteraddressmanually.submitbutton = Next
acquire_enteraddressmanually.addresswarning = If your address is missing from this postcode package or is showing incorrect details then please select this link to
acquire_enteraddressmanually.addresswarninglinktext = inform the Royal Mail.

# EnterAddressManually form errors
addressAndPostcode.addressLines.buildingNameOrNumber = Building name or number
addressAndPostcode.addressLines = Address

#keeper still on record (no previous disposal has been done to the vehicle so the keeper is still on record)
acquire_keeperstillonrecord.title = A keeper is on the record
acquire_keeperstillonrecord.text1 = This vehicle still has a keeper on record and does not appear to have been sold to the motor trade.
acquire_keeperstillonrecord.text2 = If you think this is an error, please contact DVLA customer services on 0300 790 6802 (please enter code 756 when prompted). This telephone number is open Monday to Friday: 08.00 - 19.00 and Saturday: 08.00 - 14.00.
acquire_keeperstillonrecord.text3 = You can notify us of the vehicle''s sale to the motor trade using our online service -
acquire_keeperstillonrecord.text4 = Bought a vehicle into the trade.
acquire_keeperstillonrecord.link = https://www.gov.uk/sold-bought-vehicle/y/motor-trader/bought-a-vehicle
acquire_keeperstillonrecord.text5 = Before you do this, ensure you have the consent of the keeper we currently have on record.
acquire_keeperstillonrecord.button1 = Notify another sale
acquire_keeperstillonrecord.button2 = Finish

#keeper still on record (no previous disposal has been done to the vehicle so the keeper is still on record)
error_suppressedV5C.title = Vehicle is part of a suppressed fleet
error_suppressedV5C.text1 = DVLA cannot record the sale of this vehicle as it is part of a suppressed fleet.
error_suppressedV5C.text2 = Before a sale of this vehicle can be updated, you will need to request a V5C registration certificate (logbook) from the DVLA. This can be done using the
error_suppressedV5C.text3 = view vehicle record service.
error_suppressedV5C.link = https://www.gov.uk/dvla-fleet-vehicle-info
error_suppressedV5C.button1 = Notify another sale
error_suppressedV5C.button2 = Finish

#private keeper details
acquire_privatekeeperdetails.title = Enter new keeper details
acquire_privatekeeperdetails.submitbutton = Next
acquire_privatekeeperdetails.firstnamelabel = First name(s)
acquire_privatekeeperdetails.lastnamelabel =Last name
acquire_privatekeeperdetails.emaillabel = Email address of new keeper
acquire_privatekeeperdetails.p1 = We will use these details to create the new V5C registration certificate (logbook).
acquire_privatekeeperdetails.drivernumberlabel = Driving licence number of new keeper
acquire_privatekeeperdetails.drivernumber.hint = For example, MORGA657054SM9IJ
acquire_privatekeeperdetails.postcodelabel = Postcode
acquire_privatekeeperdetails.postcode.hint = e.g. AB1 2BA or AB12BA
acquire_privatekeeperdetails.dateofbirth.hint = For example, 01 03 1976
privatekeeper_dateofbirth = Date of Birth
mandatory-alternative.not-selected = This field is mandatory. Please select one of the options.

#business keeper details
acquire_businesskeeperdetails.title = Enter new keeper details
acquire_businesskeeperdetails.p1 = We will use these details to create the new V5C registration certificate (logbook).
acquire_businesskeeperdetails.fleetnumber = Fleet number
acquire_businesskeeperdetails.businessname = Business name
acquire_businesskeeperdetails.businessname.hint = If the business buying this vehicle has a DVLA fleet number, please provide it
acquire_businesskeeperdetails.email = Email address of new keeper
acquire_businesskeeperdetails.nextbutton = Next
acquire_businesskeeperdetails.postcodelabel = Postcode
acquire_businesskeeperdetails.postcode.hint = e.g. AB1 2BA or AB12BA
acquire_businesskeeperdetails.traderemailoption = Does the new keeper want an email confirmation of the sale?
acquire_businesskeeperdetails.traderemailoption.yes = Yes
acquire_businesskeeperdetails.traderemailoption.no = No
acquire_businesskeeperdetails.fleetnumberoption = Does the new keeper have a DVLA fleet number?
acquire_businesskeeperdetails.fleetnumberoption.yes = Yes
acquire_businesskeeperdetails.fleetnumberoption.no = No
businesskeeper_postcode = Business postcode
businesskeeper_email = Business email
businessName = Business name
acquire_email_not_entered = Not entered
fleetNumberOption = Fleet number
fleetNumber = Fleet number

#new keeper choose your address
acquire_newKeeperChooseYourAddress_title = Select new keeper address
acquire_newKeeperChooseYourAddress_newKeeperName_label = Name
acquire_newKeeperChooseYourAddress_newKeeperName = {0}
acquire_newKeeperChooseYourAddress_newKeeperEmail_label = Email
acquire_newKeeperChooseYourAddress.emailNotEntered = Not entered
acquire_newKeeperChooseYourAddress_newKeeperpostcode_label = Postcode
acquire_newKeeperChooseYourAddress_addressSelect_label = Choose the new keeper's address
acquire_newKeeperChooseYourAddress.submitbutton = Next
acquire_newKeeperChooseYourAddress.manualAddressEntry = Address is not in the list
acquire_newKeeperChooseYourAddress.manualAddressEntry.enterManuallyButton = Enter address
acquire_newKeeperChooseYourAddress_noaddressesfound.h2.1 = No addresses found for that postcode.
acquire_newKeeperChooseYourAddress_noaddressesfound.h2.2 = Please either ''Enter address'' or click ''Back'' to try again.
newKeeperChooseYourAddress_addressSelect = Choose the new keepers address
change_keeper_newKeeperChooseYourAddress.address.required = Please select a valid address
businesskeeper_option_email = Email confirmation

#[optional] - new keeper manual address entry
acquire_newKeeperEnterAddressManually.title = Enter keeper address
acquire_newKeeperEnterAddressManually.submitbutton = Next

#vehicle tax or sorn
acquire_vehicleTaxOrSorn_title = Vehicle tax or SORN
acquire_vehicleTaxOrSorn.newkeeperDetails = New keeper details
acquire_vehicleTaxOrSorn.displayName = Name
acquire_vehicleTaxOrSorn.address = Address
acquire_vehicleTaxOrSorn.email = Email
acquire_vehicleTaxOrSorn.emailNotEntered = Not entered
acquire_vehicleTaxOrSorn.fleetNumber = Fleet number
acquire_vehicleTaxOrSorn.fleetNumberNotEntered = Not entered
acquire_vehicleTaxOrSorn.taxVehicle = Tax Vehicle
acquire_vehicleTaxOrSorn.sornNow = SORN now
acquire_vehicleTaxOrSorn.neither = Neither
acquire_vehicleTaxOrSorn.p1 = The vehicle needs to be taxed or a statutory off road notification (SORN) made when it is purchased.
acquire_vehicleTaxOrSorn.link = Tax the vehicle now
acquire_vehicleTaxOrSorn.taxText = This will open up a new window. To tax, you will need to use the document reference number from the V5C/2 (green slip) section of the registration certificate as a new keeper.
acquire_vehicleTaxOrSorn.taxText-2 = Once taxing is completed please return to this page and select 'Next' to continue.
acquire_vehicleTaxOrSorn.neitherText.p1 = If you have not already taxed or declared SORN for this vehicle, you must remind the new keeper that it's their responsibility to ensure the vehicle is taxed if it is driven on the road.
acquire_vehicleTaxOrSorn.neitherText.p2 = If the vehicle is being kept off road, the new keeper must make a SORN.
acquire_vehicleTaxOrSorn.submitbutton = Next
acquire_vehicleTaxOrSorn.label = Do you want to:

#complete and confirm
acquire_completeAndConfirm.title = Complete and confirm
acquire_completeandconfirm.newkeeperDetails = New keeper details
acquire_completeandconfirm.displayName = Name
acquire_completeandconfirm.address = Address
acquire_completeandconfirm.email = Email
acquire_completeandconfirm.fleetNumber = Fleet number
acquire_completeandconfirm.fleetNumberNotEntered = Not entered
acquire_completeAndConfirm.mileage.label = Vehicle mileage
acquire_completeAndConfirm.submitbutton = Confirm new keeper
acquire_completeAndConfirm.backbutton = Back
acquire_privatekeeperdetailscomplete.mileage.validation = You must enter a valid mileage between 0 and 999999
acquire_completeAndConfirm.dateofsale.label = Date of sale
acquire_completeandconfirm.dateofsale.hint = For example, 02 01 2015
acquire_completeandconfirm.dateofsale.warning = The date you’ve entered is before the current keeper acquired the vehicle, which is shown on the V5C registration certificate (logbook). Please enter a correct date that’s later than the one shown on the logbook and select ‘Next’.
acquire_completeandconfirm.dateofsale.warning.over12Months = The date you have entered is over 12 months ago, please check the date to make sure it is correct.

dateofsale = Date of sale

acquire_keeperdetailscomplete.consent = I have the consent of the party named to notify DVLA.
acquire_keeperdetailscomplete.consentError = You must have the consent of the new keeper to notify DVLA of the purchase of this vehicle

#acquire success
acquire_success.complete.header = Thank you
acquire_success.complete.p1 = The application is being processed
acquire_success.complete.p2 = Give the V5C/2 (green slip) to the new keeper and destroy the rest of the V5C registration certificate (logbook).
acquire_success.transaction_details.header = Transaction details
acquire_success.transaction_details.transaction_id = Transaction ID
acquire_success.transaction_details.transaction_date = Transaction date
acquire_success.trader_details.header = Trader details
acquire_success.trader_details.trader_name = Trader name
acquire_success.trader_details.trader_address = Trader address
acquire_success.trader_details.trader_email = Email
acquire_success.date_of_sale.label = Date of sale
acquire_success.vehicle_details.taxOrSorn = Tax or SORN
acquire_success.vehicle_details.sornEntered = Yes, SORN from today ({0}).
acquire_success.vehicle_details.sornEntered.p1 = DVLA will not send you a SORN acknowledgement letter. The SORN will be valid until the vehicle is taxed, sold, permanently exported or scrapped. Please ensure that the vehicle is taxed before it is driven on public roads.
acquire_success.vehicle_details.sornNotEntered = If the vehicle has not been taxed during this service it will need to be before being driven on the road.
acquire_success.vehicle_details.mileage = Vehicle mileage
acquire_success.vehicle_details.mileage_not_entered = Not entered
acquire_success.vehicle_details.name = Name
acquire_success.vehicle_details.address = Address
acquire_success.vehicle_details.email = Contact email address
acquire_success.new_keeper_details.p1 = New keeper details
acquire_success.further_information.header = What happens next?
acquire_success.further_information.p1 = within 5 working days we will send a new V5C registration certificate (logbook) to the new keeper.
acquire_success.further_information.p2 = if they do not receive a new V5C registration certificate (logbook) in 2 weeks they must contact DVLA customer enquiries.
acquire_success.further_information.alert = Please print this page and pass it to the customer along with the V5C/2 slip from the V5C registration certificate (logbook)

#vrm locked
vrm_locked.title = Registration number is locked
vrm_locked.text1 = There have been repeated unsuccessful attempts to access the vehicle details. The system has now locked this vehicle registration number for 10 minutes. Other vehicles can be processed during this time by selecting "Notify another sale" below.
vrm_locked.text2 = Locked at
vrm_locked.text3.1 = If you are sure that the details entered are correct then please send your completed V5C registration certificate (logbook) to
vrm_locked.text3.2 = DVLA, Swansea SA99 1BA
vrm_locked.buyanother.button = Notify another sale
vrm_locked.exit.button = Exit

error.title.unknownOption = Please enter the title of the new keeper
error.title.missing = Please enter the title of the new keeper
error.title.tooLong = Other title field can only contain characters from the following A-Z, hyphen, apostrophe, full stop and space.
error.title.illegalCharacters = Other title field can only contain characters from the following A-Z, hyphen, apostrophe, full stop and space.
error.date.notOne = Year cannot be 0000

# micro-service response codes
ms.vehiclesService.response.unableToProcessApplication-title = Transaction unsuccessful
ms.vehiclesService.response.unableToProcessApplication = We are unable to process the transaction at this time. Please send the completed V5C registration certificate (logbook) to
ms.vehiclesService.response.unableToProcessApplication-address = DVLA, Swansea, SA99 1BA

# feedback
common_feedback.subject = Buy from the trade - feedback

# email
email.template.line1 = THIS IS AN AUTOMATED EMAIL - Please do not reply as emails received at this address cannot be responded to.
email.template.line2 = For more information on driving and transport go to www.gov.uk/browse/driving
email.template.line2Html = For more information on driving and transport go to <a href="http://www.gov.uk/browse/driving" target="_blank">www.gov.uk/browse/driving</a>.
email.template.line3 = You may wish to save or print this email confirmation for your records.

email.newKeeper.p1 = DVLA have been notified electronically that you are now the new keeper of Vehicle Registration Number:
email.newKeeper.p2 = The online Transaction ID is
email.newKeeper.p3 = You should receive your new V5C registration certificate (logbook) within 2 weeks.
email.newKeeper.p4 = Since 1st October 2014, vehicle tax can no longer be transferred as part of the sale. This is because the seller will automatically receive a refund of any remaining tax.
email.newKeeper.p5 = You must tax this vehicle before it is driven on the road, tax now at http://www.gov.uk/vehicletax
email.newKeeper.p6 = If you do not want to tax you can make a SORN declaration now at http://www.gov.uk/sorn
email.newKeeper.p5Html = You must tax this vehicle before it is driven on the road, tax now at <a href="http://www.gov.uk/vehicletax" target="_blank">www.gov.uk/vehicletax</a>.
email.newKeeper.p6Html = If you do not want to tax you can make a SORN declaration now at <a href="http://www.gov.uk/sorn" target="_blank">www.gov.uk/sorn</a>.

email.trader.p1 = Thank you for using DVLA's online service to confirm you have sold this vehicle out of the motor trade. Please destroy the original V5C registration certificate (logbook) as this must not be sent to DVLA. The V5C/2 (green slip) should have been passed to the new keeper.

email.txndetails.p1 = The application details are:
email.txndetails.p2 = Vehicle Registration Number:
email.txndetails.p3 = Transaction ID is
email.txndetails.p4 = Application made on:

email.trader.p2 = The new keeper should receive their new V5C registration certificate (logbook) within 2 weeks.
email.trader.p3 = As vehicle tax or SORN can no longer be transferred as part of the sale, the new keeper must tax this vehicle before it is driven on the road at http://www.gov.uk/vehicletax. They can make a SORN at http://www.gov.uk/sorn.

email.signature.p1 = Yours sincerely
email.signature.p2 = Rohan Gye
email.signature.p3 = Vehicles Service Manager

email.title.keeper = Confirmation of new vehicle keeper
email.title.trader = Confirmation of sale
