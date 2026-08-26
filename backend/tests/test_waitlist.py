from app.services.waitlist import normalize_email


def test_lowercases_so_one_person_is_one_row():
    assert normalize_email("Felipe@Gmail.com") == "felipe@gmail.com"


def test_strips_the_whitespace_a_phone_keyboard_adds():
    assert normalize_email("  felipe@gmail.com ") == "felipe@gmail.com"
    assert normalize_email("\tfelipe@gmail.com\n") == "felipe@gmail.com"


def test_a_capitalised_first_letter_matches_the_original():
    # What actually happens on Android: the keyboard capitalises for you and
    # the person does not notice. Without this the same address signs up twice.
    assert normalize_email("Felipe@gmail.com") == normalize_email("felipe@gmail.com")


def test_leaves_the_address_otherwise_intact():
    # Dots and +tags are not ours to remove: providers are free to route
    # `a.b@` and `ab@` to different mailboxes.
    assert normalize_email("a.b+shows@dominio.com.br") == "a.b+shows@dominio.com.br"


def test_already_normal_addresses_are_unchanged():
    assert normalize_email("felipe@gmail.com") == "felipe@gmail.com"
