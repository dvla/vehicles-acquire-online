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

    var addGaEventToManualAddress = function() {
        var enterAddressManually = $('#enterAddressManuallyButton');
        if (enterAddressManually.length) {
            enterAddressManually.on('click', function() {
                _gaq.push(['_trackEvent', "manual_address", "link", "user clicked through manual address", 1]);
            });
        }

        var enterAddressManually = $('#ga-manual-address-submit');
        if (enterAddressManually.length) {
            enterAddressManually.on('click', function() {
                _gaq.push(['_trackEvent', "manual_address", "submit", "user submitted the manual address", 1]);
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
            if ($('.ga-email-option-visible').is(':checked')) {
                console.log("");
                _gaq.push(['_trackEvent', "optional_field", "trader_email", 'provided']);
            }
            if ($('.ga-email-option-invisible').is(':checked')) {
                _gaq.push(['_trackEvent',  "optional_field", "trader_email", 'absent']);
            }

        });
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
            addGaEventToManualAddress();

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
