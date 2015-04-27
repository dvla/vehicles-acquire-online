require(['config'], function() {
    require(["acquire-page-init"], function(acquirePageInit) {
        $(function() {
            acquirePageInit.init();
        });
    });
});
