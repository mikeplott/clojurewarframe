$(document).ready(function(){
    $('form').submit(function(event) {
        event.preventDefault();
        var uName = $('input [name=name]').val();
        var pass = $('input [name=pwd]').val();
        $.ajax({
            url:'/login',
            type:'POST',
            contentType:'application/json',
            data:JSON.stringify({
                username:uName,
                password:pass
            }),
            success: function(data) {
                window.location.href('/dashboard.html');
            }
        });
    });
});