// Define the dependency to page-init in common
define(['jquery', 'jquery-migrate', "page-init"], function($, jqueryMigrate, pageInit) {

    var hideEmailOnOther = function(radioOtherId, emailId) {
        if (!radioOtherId.length || !emailId.length) {
            return;
        }

        var checkStateOfRadio = function(radioOtherId, emailId) {
            //console.log("Selected radio box: " + $(radioOtherId).attr('id') + " selected;" + $(radioOtherId).attr('checked'))
            if(!$(radioOtherId).attr('checked')) {
                $(emailId).parent().hide().removeClass('item-visible');
                $(emailId).val('');
            } else {
                $(emailId).parent().show().addClass('item-visible');
            }
        };

        checkStateOfRadio(radioOtherId, emailId);

        $("input:radio" ).click(function() {
            checkStateOfRadio(radioOtherId, emailId);
        });
    };

    var addGoogleAnalyticsEventToTaxLink = function() {
        //Tracking events for Tax/SORN interactions
        if ($('#tax_details a').length) {
console.log("Adding google analytics event to tax link")
            var taxLink = $('#tax_details a');
            taxLink.on('click', function() {
                _gaq.push(['_setCustomVar', 1, 'taxsorn', 'tax_through', 3]);
                _gaq.push(['_trackEvent', 'taxsorn', 'tax_through']);
            });
        }
    };

    return {
        init: function() {
            pageInit.initAll();

            hideEmailOnOther('#privatekeeper_title_titleOption_4', '.form-item #privatekeeper_title_titleText');
            hideEmailOnOther('#tax', '#tax_details');
            hideEmailOnOther('#sorn', '#sorn_details');
            hideEmailOnOther('#neither', '#neither_details');

            addGoogleAnalyticsEventToTaxLink();
        }
    }
});
