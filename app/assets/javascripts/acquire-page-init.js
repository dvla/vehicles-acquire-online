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
            var taxLink = $('#tax_details a');
            taxLink.on('click', function() {
                _gaq.push(['_setCustomVar', 1, 'taxsorn', 'tax_through', 3]);
                _gaq.push(['_trackEvent', 'taxsorn', 'tax_through']);
            });
        }
    };

    var imageHintToggles = function() {
        $('.hint-image-wrap > .panel-indent-wrapper').hide();

        $('.hint-image-wrap > p').on('click', function() {
            $(this).siblings().toggle();
        });
    };

    var disableSubmitOnClick = function() {
        // Remove all click handlers and replace with the one below
        // This is a workaround to having common add a click handler that prevents this one from submitting any page
        // TODO: Gio to review what's been done here
        $('button[type="submit"]').off('click');
        $('button[type="submit"]').on('click', function(e) {
            console.log("acquire disable submit");

            //Tracking events for SORN checkbox submit
            var sornInputChecked = $('#sornVehicle').is(':checked');
            if (sornInputChecked) {
                _gaq.push(['_setCustomVar', 1, 'taxsorn', 'sorn', 3]);
                _gaq.push(['_trackEvent', 'taxsorn', 'sorn']);
            }
/*
            if ( $(this).hasClass("disabled") ) {
                return false;
            }

            $(this).html('Loading').addClass('loading-action disabled');
            var runTimes = 0;
            setInterval(function() {
                if ( runTimes < 3 ){
                    $('button[type="submit"]').append('.');
                    runTimes++;
                } else {
                    runTimes = 0;
                    $('button[type="submit"]').html('Loading');
                }
            }, 1000);
*/
        });
    };

    return {
        init: function() {
            // Call initAll on the pageInit object to run all the common js in vehicles-presentation-common
            pageInit.initAll();

            addGoogleAnalyticsEventToTaxLink();

            hideEmailOnOther('#privatekeeper_title_titleOption_4', '.form-item #privatekeeper_title_titleText');
            hideEmailOnOther('#tax', '#tax_details');
            hideEmailOnOther('#sorn', '#sorn_details');
            hideEmailOnOther('#neither', '#neither_details');

            imageHintToggles();

            disableSubmitOnClick();

            // SORN form reset
            $('.sorn-tax-radio-wrapper label input').on('click', function() {
                var targetSorn = $(this).attr('id');
                if (targetSorn != "sorn") {
                    $('#sornVehicle').removeAttr("checked");
                }
            });
        }
    }
});
