// Define the dependency to page-init in common
define(['jquery', 'jquery-migrate', "page-init"], function($, jqueryMigrate, pageInit) {

    var addGaEventToTaxLink = function() {
        //Tracking events for Tax/SORN interactions
        if ($('#tax_details a').length) {
            var taxLink = $('#tax_details a');
            taxLink.on('click', function() {
                _gaq.push(['_setCustomVar', 1, 'taxsorn', 'tax_through', 3]);
                _gaq.push(['_trackEvent', 'taxsorn', 'tax_through']);
            });
        }
    };

    var enableSendingGaEventsOnSubmit = function() {
        $('button[type="submit"]').on('click', function(e) {
            //Tracking events for SORN checkbox submit
            var sornInputChecked = $('#sornVehicle').is(':checked');
            if (sornInputChecked) {
                _gaq.push(['_setCustomVar', 1, 'taxsorn', 'sorn', 3]);
                _gaq.push(['_trackEvent', 'taxsorn', 'sorn']);
            }

            // tracking the optional email fields on the trader details page
            trackingOptionalRadioField("#traderEmailOption", "trader_email");
            trackingOptionalRadioField("#privatekeeper_option_email", "private_keeper_email");
            trackingOptionalRadioField("#businesskeeper_option_email", "business_keeper_email");
            trackingOptionalRadioField("#fleetNumberOption", "fleet_number");

            //track driving licence number
            trackingOptionalFields("#privatekeeper_drivernumber", "driving_licence");
            //track mileage
            trackingOptionalFields("#mileage", "mileage");

            //track date of birth
            trackingDateFields("#privatekeeper_dateofbirth", "date_of_birth");

            // track if the new owner is a business or an individual
            trackPrivateBusiness();

        });
    };

    var trackPrivateBusiness = function() {
        if ($("#vehicleSoldTo_Private").is(':checked')) {
            _gaq.push(['_trackEvent', "track_path", "individual", '', value]);
        }
        if ($("#vehicleSoldTo_Business").is(':checked')) {
            _gaq.push(['_trackEvent', "track_path", "business", '', value]);
        }

    };

    // docs here
    var trackingOptionalRadioField = function(fieldSelector, actionName, value) {
        var visibleField = $(fieldSelector + "_visible");
        var invisibleField = $(fieldSelector + "_invisible");

        if (value === undefined) value = 1;

        if (visibleField.is(':checked')) {
            _gaq.push(['_trackEvent', "optional_field", actionName, 'provided', value]);
        }
        if (invisibleField.is(':checked')) {
            _gaq.push(['_trackEvent',  "optional_field", actionName, 'absent', value]);
        }
    };

    var trackingOptionalFields = function(fieldSelector, actionName, value) {
        if (value === undefined) value = 1;

        if($(fieldSelector).value == "") {
            _gaq.push(['_trackEvent', "optional_field", actionName, 'absent', value]);
        } else {
            _gaq.push(['_trackEvent', "optional_field", actionName, 'provided', value]);
        }
    };

    var trackingDateFields = function(fieldSelector, actionName, value) {

        if (value === undefined) value = 1;

        var field_day = $(fieldSelector + "_day");
        var field_month = $(fieldSelector + "_month");
        var field_year = $(fieldSelector + "_year");


        if(field_day.value == "" || field_month.value == "" || field_year.value == "") {
            _gaq.push(['_trackEvent', "optional_field", actionName, 'absent', value]);
        } else {
            _gaq.push(['_trackEvent', "optional_field", actionName, 'provided', value]);
        }
    };


    return {
        init: function() {
            enableSendingGaEventsOnSubmit();

            // Run the common code that is not common to all but this exemplar needs
            pageInit.hideEmailOnOther('#privatekeeper_title_titleOption_4', '.form-item #privatekeeper_title_titleText');
            pageInit.hideEmailOnOther('#tax', '#tax_details');
            pageInit.hideEmailOnOther('#sorn', '#sorn_details');
            pageInit.hideEmailOnOther('#neither', '#neither_details');

            addGaEventToTaxLink();

            // SORN form reset
            $('.sorn-tax-radio-wrapper label input').on('click', function() {
                var targetSorn = $(this).attr('id');
                if (targetSorn != "sorn") {
                    $('#sornVehicle').removeAttr("checked");
                }
            });

            // Call initAll on the pageInit object to run all the common js in vehicles-presentation-common
            pageInit.initAll();
        }
    }
});
