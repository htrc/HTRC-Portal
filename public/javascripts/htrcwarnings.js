function blacklightLoggingWarning(blacklightUrl) {

    if (localStorage['dont-show-create-warning']) {
        location.href = blacklightUrl;
        return true;
    }

    $('#modal-wb-url').attr("href", blacklightUrl);
    $('#create-collection-modal').modal('show');

    return false;
}

function goToCreateCollection() {
    if ($("#dont-show").is(':checked')) {
        localStorage['dont-show-create-warning'] = true;
    }

    return true;
}

