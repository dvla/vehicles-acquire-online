require.config({
    paths: {
        'jquery': 'lib/jquery/jquery-1.9.1.min',
        'jquery-migrate': 'lib/jquery/jquery-migrate-1.2.1.min',
        'header-footer-only': 'header-footer-only',
        'form-checked-selection': 'form-checked-selection'
    },
    optimize: "none"
});

require(["jquery", "jquery-migrate", "header-footer-only", "form-checked-selection"],function($) {

    var IE10 = (navigator.userAgent.match(/(MSIE 10.0)/g) ? true : false);
    if (IE10) {
        $('html').addClass('ie10');
    }


    var hideEmailOnOther = function(radioOtherId, emailId) {

        if (!radioOtherId.length || !emailId.length) {
            return;
        }

        var checkStateOfRadio = function(radioOtherId, emailId) {
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

    var openFeedback = function(inputId, event) {
        var element = document.getElementById(inputId);
        if (element) {
            if (element.addEventListener) {
                // addEventListener is a W3 standard that is implemented in the majority of other browsers (FF, Webkit, Opera, IE9+)
                element.addEventListener(event, function (e) {
                    console.log("openFeedback addEventListener id: " + inputId + ", event " + event);
                    //window.open(url,'_blank');
                    window.open(this.href, '_blank');
                    e.preventDefault();
                });
            } else if (element.attachEvent) {
                // attachEvent can only be used on older trident rendering engines ( IE5+ IE5-8*)
                element.attachEvent(event, function (e) {
                    // console.log("openFeedback addEventListener id: " + inputId + ", event " + event);
                    //window.open(url,'_blank');
                    window.open(this.href, '_blank');
                    e.preventDefault();
                });
            } else {
                console.error("element does not support addEventListener or attachEvent");
                return false;
            }
        } else {
            console.error("element id: " + inputId + " not found on page");
            return false;
        }
    };



    $(function() {

        // Enabling loading class/js animation on submit's CTAs
        $(':submit').on('click', function(e) {
            var runTimes;

            if ( $(this).hasClass("disabled") ) {
                return false;
            }

            $(this).html('Loading').addClass('loading-action disabled');
            runTimes = 0;
            setInterval(function() {
                if ( runTimes < 3 ){
                    $(':submit').append('.');
                    runTimes++;
                } else {
                    runTimes = 0;
                    $(':submit').html('Loading');
                }
            }, 1000);
        });


        // Auto-tab for date format forms
        $('.form-date input').one('focus', function() {

            var nextInput, focusMaxLength, currentLength;
            // Getting next field
            nextInput = $(':input:eq(' + ($(':input').index(this) + 1) + ')');
            // Getting focus max length
            focusMaxLength = $(this).attr('maxlength');
            // On keyup function
            $(this).on('keyup', function(e) {
                // Getting keycode from event
                keyCode = e.keyCode || e.which;
                // If back-tab (shift+tab)
                if ((keyCode == 9) && (e.shiftKey)){
                    // browse backwards through the form and empty input content
                    $(':input:eq(' + ($(':input').index(this)) + ')').focus().val('');
                    return false;
                }
                // check if limit has been reached and move to next input
                else
                {
                    currentLength = $(this).val().length;
                    if (focusMaxLength == currentLength){
                        nextInput.focus();
                        return false;
                    }
                }
            });
        });

        hideEmailOnOther('#privatekeeper_title_titleOption_4', '.form-item #privatekeeper_title_titleText');

        hideEmailOnOther('#tax', '#tax_details');
        hideEmailOnOther('#sorn', '#sorn_details');
        hideEmailOnOther('#neither', '#neither_details');

        if ($('#feedback-open').length) {
            openFeedback('feedback-open', 'click');
        }

        // SORN form reset
        $('.sorn-tax-radio-wrapper label input').on('click', function() {
            var targetSorn = $(this).attr('id');
            if (targetSorn != "sorn") {
                $('#sornVehicle').removeAttr("checked");
            }
        });

        //html5 autofocus fallback for browsers that do not support it natively
        //if form element autofocus is not active, autofocus
        $('[autofocus]:not(:focus)').eq(0).focus();

        // Disabled clicking on disabled buttons
        $('.button-not-implemented').click(function() {
            return false;
        });

        // Print button
        $('.print-button').click(function() {
            window.print();
            return false;
        });

        // smooth scroll
        $('a[href^="#"]').bind('click.smoothscroll', function (e) {
            e.preventDefault();
            var target = this.hash,
                $target = $(target);
            $('html, body').animate({
                scrollTop: $(target).offset().top - 40
            }, 750, 'swing', function () {
                window.location.hash = target;
            });
        });

        //$(":submit").click(function() {
        //   if($(this).hasClass("disabled")) return false;
        //   $(this).addClass("disabled");
        //   return true;
        //});

        // Feedback form character countdown

        function updateCountdown() {
            // 500 is the max message length
            var remaining = 500 - $('#feedback-form textarea').val().length;
            $('.character-countdown').text(remaining + ' characters remaining.');
        }
        $(document).ready(function($) {
            // IE 9- maxlenght on input textarea
            var txts = document.getElementsByTagName('TEXTAREA')
            for(var i = 0, l = txts.length; i < l; i++) {
                if(/^[0-9]+$/.test(txts[i].getAttribute("maxlength"))) {
                    var func = function() {
                        var len = parseInt(this.getAttribute("maxlength"), 10);

                        if(this.value.length > len) {
                            this.value = this.value.substr(0, len);
                            return false;
                        }
                    }
                    txts[i].onkeyup = func;
                    txts[i].onblur = func;
                }
            }
            // Update Countdown on input textarea
            $('#feedback-form textarea').change(updateCountdown);
            $('#feedback-form textarea').keyup(updateCountdown);
        });

        // Radio button toggle visible widget
        $('.optional-field').hide();

        $('.expandable-optional .option-visible').on('click', function() {
            $(this).closest('.expandable-optional').find('.optional-field').show(100);
        });
        $('.expandable-optional .option-invisible').on('click', function() {
            $(this).closest('.expandable-optional').find('.optional-field').hide(100);
        });

        $('.expandable-optional .option-visible:checked').click();

    });

    function areCookiesEnabled(){
        var cookieEnabled = (navigator.cookieEnabled) ? true : false;

        if (typeof navigator.cookieEnabled == "undefined" && !cookieEnabled)
        {
            document.cookie="testcookie";
            cookieEnabled = (document.cookie.indexOf("testcookie") != -1) ? true : false;
        }
        return (cookieEnabled);
    }

    function opt(v){
        if (typeof v == 'undefined') return [];
        else return[v];
    }

}); // end require
