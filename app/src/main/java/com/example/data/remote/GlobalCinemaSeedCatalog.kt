package com.example.data.remote

import com.example.data.model.CastMember
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ReleaseStatus

object GlobalCinemaSeedCatalog {

  // Actors with cross-referenced IDs so clicking any actor shows all their movies & series
  val SRK = CastMember(
    id = "actor_srk",
    name = "Shah Rukh Khan",
    characterName = "Vikram Rathore / Azad / Pathaan",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6e/Shah_Rukh_Khan_graces_the_launch_of_the_new_Santro.jpg/440px-Shah_Rukh_Khan_graces_the_launch_of_the_new_Santro.jpg",
    bio = "Acclaimed Indian actor and film producer known as the 'King of Bollywood', starring in over 90 films across action, romance, and drama."
  )

  val DEEPIKA = CastMember(
    id = "actor_deepika",
    name = "Deepika Padukone",
    characterName = "SUM-80 / Rubai / Aishwarya",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Deepika_Padukone_Cannes_2018_%28cropped%29.jpg/440px-Deepika_Padukone_Cannes_2018_%28cropped%29.jpg",
    bio = "Leading Indian actress starring in major Bollywood and pan-Indian blockbusters including Kalki 2898 AD, Jawan, and Pathaan."
  )

  val PRABHAS = CastMember(
    id = "actor_prabhas",
    name = "Prabhas",
    characterName = "Bhairava / Deva / Shivudu",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/Prabhas_by_Gage_Skidmore.jpg/440px-Prabhas_by_Gage_Skidmore.jpg",
    bio = "Pan-Indian superstar known for Baahubali, Salaar, Kalki 2898 AD, and Spirit."
  )

  val ALLU_ARJUN = CastMember(
    id = "actor_allu_arjun",
    name = "Allu Arjun",
    characterName = "Pushpa Raj",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/Allu_Arjun_at_69th_National_Film_Awards.jpg/440px-Allu_Arjun_at_69th_National_Film_Awards.jpg",
    bio = "National Film Award-winning Indian actor celebrated for the Pushpa franchise and stylish action cinema."
  )

  val RANBIR = CastMember(
    id = "actor_ranbir",
    name = "Ranbir Kapoor",
    characterName = "Lord Rama / Ranvijay / Shiva",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d2/Ranbir_Kapoor_promoting_Brahmastra.jpg/440px-Ranbir_Kapoor_promoting_Brahmastra.jpg",
    bio = "Versatile Indian actor starring in Ramayana, Animal, Brahmastra, and Barfi."
  )

  val ALIA = CastMember(
    id = "actor_alia",
    name = "Alia Bhatt",
    characterName = "Isha / Sita / Safeena",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/Alia_Bhatt_at_Berlinale_2022_cropped.jpg/440px-Alia_Bhatt_at_Berlinale_2022_cropped.jpg",
    bio = "National Award-winning actress known for RRR, Gangubai Kathiawadi, Brahmastra, and Alpha."
  )

  val DICAPRIO = CastMember(
    id = "actor_dicaprio",
    name = "Leonardo DiCaprio",
    characterName = "Dom Cobb / Teddy Daniels",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Leonardo_Dicaprio_Cannes_2019.jpg/440px-Leonardo_Dicaprio_Cannes_2019.jpg",
    bio = "Academy Award-winning American actor and film producer known for Inception, The Revenant, and Shutter Island."
  )

  val CILLIAN = CastMember(
    id = "actor_cillian",
    name = "Cillian Murphy",
    characterName = "J. Robert Oppenheimer / Robert Fischer / Thomas Shelby",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Cillian_Murphy_at_Berlinale_2024%2C_Ausschnitt.jpg/440px-Cillian_Murphy_at_Berlinale_2024%2C_Ausschnitt.jpg",
    bio = "Oscar-winning Irish actor renowned for Oppenheimer, Peaky Blinders, and Inception."
  )

  val RDJ = CastMember(
    id = "actor_rdj",
    name = "Robert Downey Jr.",
    characterName = "Tony Stark / Doctor Doom / Lewis Strauss",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/94/Robert_Downey_Jr_2014_Comic_Con_%28cropped%29.jpg/440px-Robert_Downey_Jr_2014_Comic_Con_%28cropped%29.jpg",
    bio = "Academy Award-winning actor famous for Iron Man, Oppenheimer, and Avengers: Doomsday."
  )

  val ZENDAYA = CastMember(
    id = "actor_zendaya",
    name = "Zendaya",
    characterName = "Chani / MJ",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/Zendaya_-_2019_by_Glenn_Francis.jpg/440px-Zendaya_-_2019_by_Glenn_Francis.jpg",
    bio = "Emmy-winning actress and star of the Dune franchise, Spider-Man, and Euphoria."
  )

  val TIMOTHEE = CastMember(
    id = "actor_timothee",
    name = "Timothee Chalamet",
    characterName = "Paul Atreides",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Timoth%C3%A9e_Chalamet_2017_Berlinale.jpg/440px-Timoth%C3%A9e_Chalamet_2017_Berlinale.jpg",
    bio = "Academy Award-nominated actor known for Dune: Part One, Dune: Part Two, and Dune: Messiah."
  )

  val PEDRO = CastMember(
    id = "actor_pedro",
    name = "Pedro Pascal",
    characterName = "Joel Miller / Reed Richards",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Pedro_Pascal_by_Gage_Skidmore.jpg/440px-Pedro_Pascal_by_Gage_Skidmore.jpg",
    bio = "Internationally acclaimed actor starring in The Last of Us, The Mandalorian, and Avengers: Doomsday."
  )

  val MANOJ = CastMember(
    id = "actor_manoj",
    name = "Manoj Bajpayee",
    characterName = "Srikant Tiwari",
    profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/63/Manoj_Bajpayee_at_52nd_IFFI.jpg/440px-Manoj_Bajpayee_at_52nd_IFFI.jpg",
    bio = "Three-time National Film Award winner celebrated for The Family Man, Satya, and Gangs of Wasseypur."
  )

  val initialCatalog: List<MediaItem> = listOf(
    // --- RELEASED MOVIES ---
    MediaItem(
      id = "curated_kalki_2898",
      title = "Kalki 2898 AD",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2024",
      releaseDateFull = "27 June 2024",
      rating = 8.4,
      posterUrl = "https://i.ytimg.com/vi/kQDd1AhGIHk/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/aninoDcPWo4/hqdefault.jpg",
      overview = "In a post-apocalyptic world in the year 2898 AD, bounty hunter Bhairava crosses paths with Ashwatthama and SUM-80 in a mythic battle to protect the unborn avatar Kalki.",
      genres = listOf("Sci-Fi", "Action", "Mythology"),
      language = "Telugu / Hindi / English",
      durationOrSeasons = "3h 01m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        PRABHAS.copy(characterName = "Bhairava / Karna"),
        DEEPIKA.copy(characterName = "SUM-80 (Sumathi)"),
        CastMember(
          id = "actor_amitabh",
          name = "Amitabh Bachchan",
          characterName = "Ashwatthama",
          profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Indian_actor_Amitabh_Bachchan.jpg/440px-Indian_actor_Amitabh_Bachchan.jpg",
          bio = "Legendary Indian cinema icon spanning over five decades of landmark films."
        )
      ),
      director = "Nag Ashwin"
    ),
    MediaItem(
      id = "curated_jawan",
      title = "Jawan",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2023",
      releaseDateFull = "7 September 2023",
      rating = 8.1,
      posterUrl = "https://i.ytimg.com/vi/COv52Qyctws/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/MWOlnZSnXJo/hqdefault.jpg",
      overview = "A high-octane action thriller that outlines the emotional journey of a man who is set to rectify the wrongs in society with his fearless women's squad.",
      genres = listOf("Action", "Thriller", "Drama"),
      language = "Hindi / Tamil / Telugu",
      durationOrSeasons = "2h 49m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        SRK.copy(characterName = "Vikram Rathore & Azad"),
        DEEPIKA.copy(characterName = "Aishwarya Rathore")
      ),
      director = "Atlee"
    ),
    MediaItem(
      id = "curated_pushpa_2",
      title = "Pushpa 2: The Rule",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2024",
      releaseDateFull = "5 December 2024",
      rating = 8.6,
      posterUrl = "https://i.ytimg.com/vi/1kVK0MZlbI4/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/Q1NKMPhP8PY/hqdefault.jpg",
      overview = "Pushpa Raj expands his red sandalwood syndicate globally while facing a fierce showdown with SP Bhanwar Singh Shekhawat.",
      genres = listOf("Action", "Crime", "Drama"),
      language = "Telugu / Hindi",
      durationOrSeasons = "3h 20m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        ALLU_ARJUN.copy(characterName = "Pushpa Raj"),
        CastMember(
          id = "actor_rashmika",
          name = "Rashmika Mandanna",
          characterName = "Srivalli",
          profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Rashmika_Mandanna_at_Animal_promotions.jpg/440px-Rashmika_Mandanna_at_Animal_promotions.jpg",
          bio = "Popular Indian actress known for Pushpa, Animal, and Sita Ramam."
        )
      ),
      director = "Sukumar"
    ),
    MediaItem(
      id = "curated_sita_ramam",
      title = "Sita Ramam",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2022",
      releaseDateFull = "5 August 2022",
      rating = 8.6,
      posterUrl = "https://i.ytimg.com/vi/Ljk6tGZ1l3A/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/3UKsbXQUwqw/hqdefault.jpg",
      overview = "An orphan soldier, Lieutenant Ram's life changes after he receives a heartfelt letter from a girl named Sita, sparking an unforgettable timeless romantic drama across borders.",
      genres = listOf("Romance", "Drama", "History"),
      language = "Telugu / Hindi / Tamil",
      durationOrSeasons = "2h 43m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CastMember(
          id = "actor_dulquer",
          name = "Dulquer Salmaan",
          characterName = "Lieutenant Ram",
          profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/Dulquer_Salmaan_at_Zoya_Factor_Trailer_Launch.jpg/440px-Dulquer_Salmaan_at_Zoya_Factor_Trailer_Launch.jpg",
          bio = "Acclaimed pan-Indian actor known for Sita Ramam, Mahanati, and Lucky Baskhar."
        ),
        CastMember(
          id = "actor_mrunal",
          name = "Mrunal Thakur",
          characterName = "Princess Noor Jahan / Sita Mahalakshmi",
          profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/39/Mrunal_Thakur_snapped_at_Maddock_office.jpg/440px-Mrunal_Thakur_snapped_at_Maddock_office.jpg",
          bio = "Celebrated actress known for Sita Ramam, Hi Nanna, and Super 30."
        ),
        CastMember(
          id = "actor_rashmika",
          name = "Rashmika Mandanna",
          characterName = "Afreen",
          profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Rashmika_Mandanna_at_Animal_promotions.jpg/440px-Rashmika_Mandanna_at_Animal_promotions.jpg",
          bio = "Popular Indian actress known for Pushpa, Animal, and Sita Ramam."
        )
      ),
      director = "Hanu Raghavapudi"
    ),
    MediaItem(
      id = "curated_veer_zaara",
      title = "Veer-Zaara",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2004",
      releaseDateFull = "12 November 2004 (Re-Released 2024)",
      rating = 8.5,
      posterUrl = "https://i.ytimg.com/vi/OSaVImLnnsw/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/rp5L8I2wMZ8/hqdefault.jpg",
      overview = "Indian Air Force pilot Veer Pratap Singh rescues Zaara, a woman from Pakistan, leading to an epic romantic drama of love, sacrifice, and 22 years of devotion.",
      genres = listOf("Romance", "Drama", "Musical"),
      language = "Hindi",
      durationOrSeasons = "3h 12m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        SRK.copy(characterName = "Squadron Leader Veer Pratap Singh"),
        CastMember(
          id = "actor_amitabh",
          name = "Amitabh Bachchan",
          characterName = "Chaudhary Sumer Singh",
          profileUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Indian_actor_Amitabh_Bachchan.jpg/440px-Indian_actor_Amitabh_Bachchan.jpg",
          bio = "Legendary Indian cinema icon spanning over five decades of landmark films."
        )
      ),
      director = "Yash Chopra"
    ),
    MediaItem(
      id = "curated_dunki",
      title = "Dunki",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2023",
      releaseDateFull = "21 December 2023",
      rating = 7.9,
      posterUrl = "https://i.ytimg.com/vi/Zd69FfhBmSc/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/ACKQDAlAfFE/hqdefault.jpg",
      overview = "Four friends from a village in Punjab share a common dream of going to England, embarking on a heartfelt romantic drama and emotional journey of love and homecoming.",
      genres = listOf("Drama", "Romance", "Comedy"),
      language = "Hindi",
      durationOrSeasons = "2h 41m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        SRK.copy(characterName = "Hardayal 'Hardy' Singh Dhillon")
      ),
      director = "Rajkumar Hirani"
    ),
    MediaItem(
      id = "curated_rockstar",
      title = "Rockstar",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2011",
      releaseDateFull = "11 November 2011 (Re-Released 2024)",
      rating = 8.3,
      posterUrl = "https://i.ytimg.com/vi/bD5FShPZdpw/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/p9DQINKZxWE/hqdefault.jpg",
      overview = "Janardhan Jakhar chases his dream of becoming a rockstar, discovering through his intense and soulful romance with Heer that true music is born from heartbreak.",
      genres = listOf("Romance", "Drama", "Musical"),
      language = "Hindi",
      durationOrSeasons = "2h 39m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        RANBIR.copy(characterName = "Janardhan 'Jordan' Jakhar")
      ),
      director = "Imtiaz Ali"
    ),
    MediaItem(
      id = "curated_inception",
      title = "Inception",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2010",
      releaseDateFull = "16 July 2010",
      rating = 8.8,
      posterUrl = "https://i.ytimg.com/vi/YoHD9XEInc0/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/8hP9D6kZseM/hqdefault.jpg",
      overview = "A skilled extractor who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea into the mind of a CEO.",
      genres = listOf("Sci-Fi", "Action", "Thriller"),
      language = "English / Hindi Dubbed",
      durationOrSeasons = "2h 28m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        DICAPRIO.copy(characterName = "Dom Cobb"),
        CILLIAN.copy(characterName = "Robert Fischer")
      ),
      director = "Christopher Nolan"
    ),
    MediaItem(
      id = "curated_oppenheimer",
      title = "Oppenheimer",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2023",
      releaseDateFull = "21 July 2023",
      rating = 8.9,
      posterUrl = "https://i.ytimg.com/vi/uYPbbksJxIg/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/bK6ldnjE3Y0/hqdefault.jpg",
      overview = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.",
      genres = listOf("Biography", "Drama", "History"),
      language = "English / Hindi",
      durationOrSeasons = "3h 00m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CILLIAN.copy(characterName = "J. Robert Oppenheimer"),
        RDJ.copy(characterName = "Lewis Strauss")
      ),
      director = "Christopher Nolan"
    ),
    MediaItem(
      id = "curated_dune_2",
      title = "Dune: Part Two",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2024",
      releaseDateFull = "1 March 2024",
      rating = 8.7,
      posterUrl = "https://i.ytimg.com/vi/Way9Dexny3w/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/U2Qp5pL3ovA/hqdefault.jpg",
      overview = "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family and fighting for his love with Chani.",
      genres = listOf("Sci-Fi", "Adventure", "Drama", "Romance"),
      language = "English / Hindi",
      durationOrSeasons = "2h 46m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        TIMOTHEE.copy(characterName = "Paul Atreides"),
        ZENDAYA.copy(characterName = "Chani")
      ),
      director = "Denis Villeneuve"
    ),
    MediaItem(
      id = "curated_brahmastra",
      title = "Brahmastra: Part One - Shiva",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2022",
      releaseDateFull = "9 September 2022",
      rating = 7.6,
      posterUrl = "https://i.ytimg.com/vi/BUjXzrgntcY/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/BddP6PYo2gs/hqdefault.jpg",
      overview = "Shiva, a young DJ with a mystical connection to fire, falls in love with Isha and discovers the secret Astraverse and his destiny to protect the Brahmastra.",
      genres = listOf("Fantasy", "Action", "Romance", "Drama"),
      language = "Hindi",
      durationOrSeasons = "2h 47m",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        RANBIR.copy(characterName = "Shiva"),
        ALIA.copy(characterName = "Isha"),
        SRK.copy(characterName = "Mohan Bhargav (Vanarastra)")
      ),
      director = "Ayan Mukerji"
    ),

    // --- UPCOMING MOVIES ("RELEASE HONE WALI FILMEIN") ---
    MediaItem(
      id = "curated_ramayana_part1",
      title = "Ramayana: Part One",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.UPCOMING,
      releaseYear = "2026",
      releaseDateFull = "Diwali 2026 (Upcoming)",
      rating = 9.4,
      posterUrl = "https://i.ytimg.com/vi/scNmYjoR-qM/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/X7WXHhokylc/hqdefault.jpg",
      overview = "An epic big-screen adaptation of the ancient Indian epic Ramayana, chronicling the journey of Prince Rama, Sita, and Hanuman.",
      genres = listOf("Epic", "Mythology", "Action"),
      language = "Hindi / Tamil / Telugu / English",
      durationOrSeasons = "Upcoming Epic",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        RANBIR.copy(characterName = "Lord Rama"),
        ALIA.copy(characterName = "Goddess Sita")
      ),
      director = "Nitesh Tiwari"
    ),
    MediaItem(
      id = "curated_king_2026",
      title = "King",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.UPCOMING,
      releaseYear = "2026",
      releaseDateFull = "Eid 2026 (Upcoming)",
      rating = 9.1,
      posterUrl = "https://i.ytimg.com/vi/vqu4z34wENw/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/COv52Qyctws/hqdefault.jpg",
      overview = "A stylish underworld mentor takes a young protege under his wing across a dangerous international syndicate.",
      genres = listOf("Action", "Crime", "Thriller"),
      language = "Hindi",
      durationOrSeasons = "Upcoming Release",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        SRK.copy(characterName = "The King (Mentor)"),
        DEEPIKA.copy(characterName = "Special Appearance")
      ),
      director = "Siddharth Anand"
    ),
    MediaItem(
      id = "curated_avengers_doomsday",
      title = "Avengers: Doomsday",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.UPCOMING,
      releaseYear = "2026",
      releaseDateFull = "May 2026 (Upcoming)",
      rating = 9.3,
      posterUrl = "https://i.ytimg.com/vi/TcMBFSGVi1c/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/6ZfuNTqbHE8/hqdefault.jpg",
      overview = "Earth's Mightiest Heroes and the Fantastic Four unite across the Multiverse to confront Victor von Doom.",
      genres = listOf("Sci-Fi", "Action", "Superhero"),
      language = "English / Hindi",
      durationOrSeasons = "Upcoming Blockbuster",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        RDJ.copy(characterName = "Victor von Doom / Doctor Doom"),
        PEDRO.copy(characterName = "Reed Richards / Mister Fantastic")
      ),
      director = "Anthony & Joe Russo"
    ),
    MediaItem(
      id = "curated_spirit_prabhas",
      title = "Spirit",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.UPCOMING,
      releaseYear = "2027",
      releaseDateFull = "2027 (Upcoming)",
      rating = 9.0,
      posterUrl = "https://i.ytimg.com/vi/4GPvYMKtrtI/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/HihakYi5M2I/hqdefault.jpg",
      overview = "A ruthless, uncompromising police officer takes on an international crime empire in Sandeep Reddy Vanga's high-intensity action drama.",
      genres = listOf("Action", "Crime", "Thriller"),
      language = "Telugu / Hindi",
      durationOrSeasons = "Upcoming Release",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        PRABHAS.copy(characterName = "IPS Officer")
      ),
      director = "Sandeep Reddy Vanga"
    ),
    MediaItem(
      id = "curated_dune_messiah",
      title = "Dune: Messiah",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.UPCOMING,
      releaseYear = "2026",
      releaseDateFull = "December 2026 (Upcoming)",
      rating = 9.2,
      posterUrl = "https://i.ytimg.com/vi/U2Qp5pL3ovA/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/_YUzQa_1RCE/hqdefault.jpg",
      overview = "Twelve years into his reign as Emperor, Paul Atreides faces a political conspiracy while grappling with the consequences of the Fremen jihad.",
      genres = listOf("Sci-Fi", "Drama", "Adventure"),
      language = "English / Hindi",
      durationOrSeasons = "Upcoming Finale",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        TIMOTHEE.copy(characterName = "Emperor Paul Atreides"),
        ZENDAYA.copy(characterName = "Chani")
      ),
      director = "Denis Villeneuve"
    ),

    // --- GLOBAL & INDIAN SERIES ---
    MediaItem(
      id = "curated_family_man_s3",
      title = "The Family Man (All Seasons & S3)",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.UPCOMING,
      releaseYear = "2026",
      releaseDateFull = "Seasons 1-2 Released • S3 Coming Soon",
      rating = 8.9,
      posterUrl = "https://i.ytimg.com/vi/XatRGut65VI/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/NGf_B81Hc2M/hqdefault.jpg",
      overview = "Srikant Tiwari, a middle-class man secretly working as an intelligence officer for TASC, balances family life with covert national security operations.",
      genres = listOf("Action", "Spy Thriller", "Comedy"),
      language = "Hindi / English",
      durationOrSeasons = "3 Seasons",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        MANOJ.copy(characterName = "Srikant Tiwari")
      ),
      director = "Raj & DK"
    ),
    MediaItem(
      id = "curated_peaky_blinders",
      title = "Peaky Blinders",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2013",
      releaseDateFull = "6 Seasons (Complete)",
      rating = 8.8,
      posterUrl = "https://i.ytimg.com/vi/oVzVdvGIC7U/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/2nsT9uQPIrk/hqdefault.jpg",
      overview = "A gangster family epic set in 1900s England, centering on a gang who sew razor blades in the peaks of their caps, and their fierce boss Tommy Shelby.",
      genres = listOf("Crime", "Drama", "History"),
      language = "English / Hindi",
      durationOrSeasons = "6 Seasons",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CILLIAN.copy(characterName = "Thomas Shelby")
      ),
      director = "Steven Knight"
    ),
    MediaItem(
      id = "curated_last_of_us",
      title = "The Last of Us",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2023",
      releaseDateFull = "2 Seasons",
      rating = 8.8,
      posterUrl = "https://i.ytimg.com/vi/uLtkt8BonwM/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/BOsAJ7oe2QE/hqdefault.jpg",
      overview = "After a global pandemic destroys civilization, a hardened survivor takes charge of a 14-year-old girl who may be humanity's last hope.",
      genres = listOf("Drama", "Sci-Fi", "Action"),
      language = "English / Hindi",
      durationOrSeasons = "2 Seasons",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        PEDRO.copy(characterName = "Joel Miller")
      ),
      director = "Craig Mazin & Neil Druckmann"
    ),
    MediaItem(
      id = "curated_mirzapur_s3",
      title = "Mirzapur (Seasons 1-3)",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2024",
      releaseDateFull = "3 Seasons • Complete Crime Thriller",
      rating = 8.5,
      posterUrl = "https://i.ytimg.com/vi/ZNeGF-PvVHY/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/kE63q8sZg1w/hqdefault.jpg",
      overview = "A shocking incident at a wedding procession ignites a series of events entangling two families in the lawless city of Mirzapur where power and blood rule.",
      genres = listOf("Action", "Crime", "Thriller"),
      language = "Hindi / English",
      durationOrSeasons = "3 Seasons (30 Episodes)",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CastMember("actor_pankaj", "Pankaj Tripathi", "Kaleen Bhaiya", "", "Celebrated Indian actor"),
        CastMember("actor_ali", "Ali Fazal", "Guddu Pandit", "", "Indian actor")
      ),
      director = "Karan Anshuman & Gurmmeet Singh"
    ),
    MediaItem(
      id = "curated_panchayat_s3",
      title = "Panchayat (Seasons 1-3)",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2024",
      releaseDateFull = "3 Seasons • Comedy Drama",
      rating = 9.0,
      posterUrl = "https://i.ytimg.com/vi/mojZJ7oeD_g/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/e3Nl_TCQXuw/hqdefault.jpg",
      overview = "An engineering graduate takes up a job as a secretary of a Panchayat office in a remote village called Phulera in Uttar Pradesh, encountering hilarious and heartwarming rural challenges.",
      genres = listOf("Comedy", "Drama"),
      language = "Hindi",
      durationOrSeasons = "3 Seasons (24 Episodes)",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CastMember("actor_jitendra", "Jitendra Kumar", "Abhishek Tripathi (Sachiv Ji)", "", "Indian actor"),
        CastMember("actor_neena", "Neena Gupta", "Manju Devi", "", "Veteran Indian actress")
      ),
      director = "Deepak Kumar Mishra"
    ),
    MediaItem(
      id = "curated_stranger_things",
      title = "Stranger Things",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2016-2025",
      releaseDateFull = "4 Seasons Released • Final Season 5 Coming",
      rating = 8.7,
      posterUrl = "https://i.ytimg.com/vi/b9EkMc79ZSU/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/sBEvEcpnG7k/hqdefault.jpg",
      overview = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
      genres = listOf("Sci-Fi", "Horror", "Drama"),
      language = "English / Hindi",
      durationOrSeasons = "4 Seasons",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CastMember("actor_millie", "Millie Bobby Brown", "Eleven", "", "British actress")
      ),
      director = "The Duffer Brothers"
    ),
    MediaItem(
      id = "curated_sacred_games",
      title = "Sacred Games",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2019",
      releaseDateFull = "2 Seasons • Complete Thriller",
      rating = 8.6,
      posterUrl = "https://i.ytimg.com/vi/28j8h0RRnq4/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/w-Xe8gLBc5I/hqdefault.jpg",
      overview = "A link in their pasts leads an honest cop to a fugitive gang boss whose cryptic warning spurs a quest to save Mumbai from cataclysm.",
      genres = listOf("Crime", "Mystery", "Thriller"),
      language = "Hindi / English",
      durationOrSeasons = "2 Seasons (16 Episodes)",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CastMember("actor_saif", "Saif Ali Khan", "Sartaj Singh", "", "Indian actor"),
        CastMember("actor_nawaz", "Nawazuddin Siddiqui", "Ganesh Gaitonde", "", "Critically acclaimed Indian actor")
      ),
      director = "Anurag Kashyap & Vikramaditya Motwane"
    ),
    MediaItem(
      id = "curated_breaking_bad",
      title = "Breaking Bad",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2008-2013",
      releaseDateFull = "5 Seasons (62 Episodes) • All-Time Masterpiece",
      rating = 9.5,
      posterUrl = "https://i.ytimg.com/vi/HhesaQXLuRY/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/VFkxRPJmI1U/hqdefault.jpg",
      overview = "A chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine with a former student in order to secure his family's future.",
      genres = listOf("Crime", "Drama", "Thriller"),
      language = "English / Hindi",
      durationOrSeasons = "5 Seasons",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CastMember("actor_bryan", "Bryan Cranston", "Walter White / Heisenberg", "", "Award winning actor"),
        CastMember("actor_aaron", "Aaron Paul", "Jesse Pinkman", "", "American actor")
      ),
      director = "Vince Gilligan"
    ),
    MediaItem(
      id = "curated_the_boys",
      title = "The Boys",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2024",
      releaseDateFull = "4 Seasons Released • Season 5 Coming",
      rating = 8.7,
      posterUrl = "https://i.ytimg.com/vi/EzFXDvC-AZE/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/5skflk2ZlGI/hqdefault.jpg",
      overview = "A fun and irreverent take on what happens when superheroes—who are as popular as celebrities—abuse their superpowers rather than use them for good.",
      genres = listOf("Action", "Sci-Fi", "Comedy"),
      language = "English / Hindi",
      durationOrSeasons = "4 Seasons",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CastMember("actor_karl", "Karl Urban", "Billy Butcher", "", "Actor"),
        CastMember("actor_antony", "Antony Starr", "Homelander", "", "Actor")
      ),
      director = "Eric Kripke"
    ),
    MediaItem(
      id = "curated_farzi",
      title = "Farzi",
      mediaType = MediaType.SERIES,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2023",
      releaseDateFull = "Season 1 Released • Season 2 Announced",
      rating = 8.4,
      posterUrl = "https://i.ytimg.com/vi/v5O_7gG08iU/hqdefault.jpg",
      backdropUrl = "https://i.ytimg.com/vi/7v9R8j2wKxE/hqdefault.jpg",
      overview = "Sunny, a brilliant small-time artist, is catapulted into the high-stakes world of counterfeiting when he creates the perfect fake currency note.",
      genres = listOf("Crime", "Thriller", "Drama"),
      language = "Hindi / Tamil / Telugu",
      durationOrSeasons = "1 Season (8 Episodes)",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = listOf(
        CastMember("actor_shahid", "Shahid Kapoor", "Sunny", "", "Indian actor"),
        CastMember("actor_vjs", "Vijay Sethupathi", "Michael Vedanayagam", "", "Pan-Indian superstar")
      ),
      director = "Raj & DK"
    )
  )
}
