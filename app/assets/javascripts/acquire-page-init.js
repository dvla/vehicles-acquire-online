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
