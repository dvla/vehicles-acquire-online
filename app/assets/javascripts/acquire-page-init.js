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

    var addGaEventToTodayDate = function() {
        //Tracking event for Today's date functionality
        if ($('#todays_date').length) {
            var todayDate = $('#todays_date');
            todayDate.on('click', function() {
                _gaq.push(['_trackEvent',  'ct_link', 'today_date', 'click',  1]);
            });
        }
    };

    var addGaEventToV5Chint = function() {
        //Tracking event for click on V5C image tooltip
        if ($('.hint-image-wrap p').length) {
            var v5cHint = $('.hint-image-wrap p');
            v5cHint.on('click', function() {
                var currentEvent = $('.hint-image-wrap p').attr('data-tracking');
                _gaq.push(['_trackEvent', 'ct_link',  currentEvent, 'click',  1]);
            });
        }
    };

    var addGaEventToBuyAnother = function() {
        //Tracking event for click on "Buy another Vehicle"
        if ($('.success-buy-another-btn').length) {
            var buyAnother = $('.success-buy-another-btn');
            buyAnother.on('click', function() {
                var currentEvent = $('.success-buy-another-btn').attr('data-tracking');
                _gaq.push(['_trackEvent', 'ct_link',  currentEvent, 'buy_another',  1]);
            });
        }
    };

    var addGaEventToPrint = function() {
        //Tracking event for click on "Finish" button
        if ($('.print-button').length) {
            var printBtn = $('.print-button');
            printBtn.on('click', function() {
                _gaq.push(['_trackEvent', 'ct_link',  'summary_action', 'print',  1]);
            });
        }
    };

    var addGaEventToFinish = function() {
        //Tracking event for click on "Finish" button
        if ($('.success-finish-btn').length) {
            var finishBtn = $('.success-finish-btn');
            finishBtn.on('click', function() {
                var currentEvent = $('.success-finish-btn').attr('data-tracking');
                _gaq.push(['_trackEvent', 'ct_link',  currentEvent, 'finish',  1]);
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

            //ACQ-002: tracking the optional email fields on the trader details page
            trackingOptionalRadioField("#traderEmailOption", "trader_email");

            // ACQ-002: tracking the optional email on the private keeper details page
            trackingOptionalRadioField("#privatekeeper_option_email", "private_keeper_email");
            // ACQ-003: tracking the optional email on the business keeper details page
            trackingOptionalRadioField("#businesskeeper_option_email", "business_keeper_email");
            // ACQ-004: tracking the optional fleet number on the business keeper details page
            trackingOptionalRadioField("#fleetNumberOption", "fleet_number");

            //ACQ-006: tracking driving licence number on the private keeper details page
            trackingOptionalFields("#privatekeeper_drivernumber", "driving_licence");
            //ACQ-007: tracking the vehicle mileage on the sales details page
            trackingOptionalFields("#mileage", "mileage");

            //ACQ-005: tracking date of birth on the private keeper details page
            trackingDateFields("#privatekeeper_dateofbirth", "date_of_birth");

            //ACQ-011: tracking if the new owner is a business or an individual
            trackPrivateBusiness();


        });
    };

    var trackPrivateBusiness = function() {
        if ($("#vehicleSoldTo_Private").is(':checked')) {
            _gaq.push(['_trackEvent', "track_path", "individual"]);
        }
        if ($("#vehicleSoldTo_Business").is(':checked')) {
            _gaq.push(['_trackEvent', "track_path", "business"]);
        }
    };

    // tracks an event based on the state of an optional radio box. This will work for the yes/no radio boxes.
    // you need to pass the fieldSelector (for id: #id, for class: .className) and the name of the action.
    // value is optional
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

    // tracks an event based on a field that has a value. e.g. a textfield.
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
            addGaEventToTodayDate();
            addGaEventToV5Chint();
            addGaEventToBuyAnother();
            addGaEventToFinish();
            addGaEventToPrint();

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
