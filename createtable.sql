create table movies(
	id varchar(10) not null DEFAULT '',
    title varchar(100) not null DEFAULT '',
    year integer not null,
    director varchar(100) not null DEFAULT '',
    primary key(id)
);

create table stars(
	id varchar(10) not null DEFAULT '',
    name varchar(100) not null DEFAULT '',
    birthYear integer,
    primary key(id)
);

create table stars_in_movies(
	starId varchar(10) not null DEFAULT '',
    movieId varchar(10) not null DEFAULT '',
    foreign key(starId) references stars(id),
    foreign key(movieId) references movies(id)
);

create table genres(
	id integer not null auto_increment,
    name varchar(32) not null DEFAULT '',
    primary key(id)
);

create table genres_in_movies(
	genreId integer not null,
    movieId varchar(10) not null DEFAULT '',
    foreign key(genreId) references genres(id),
    foreign key(movieId) references movies(id)
);

create table creditcards(
	id varchar(20) not null DEFAULT '',
    firstName varchar(50) not null DEFAULT '',
    lastName varchar(50) not null DEFAULT '',
    expiration date not null,
    primary key(id)
);

create table customers(
	id integer not null auto_increment,
    firstName varchar(50) not null DEFAULT '',
    lastName varchar(50) not null DEFAULT '',
    ccId varchar(20) not null DEFAULT '',
    address varchar(200) not null DEFAULT '',
    email varchar(50) not null DEFAULT '',
    password varchar(20) not null DEFAULT '',
    primary key(id),
    foreign key(ccId) references creditcards(id)
);

create table sales(
	id integer not null auto_increment,
    customerId integer not null,
    movieId varchar(10) not null DEFAULT '',
    saleDate date not null,
    primary key(id),
    foreign key(customerId) references customers(id),
    foreign key(movieId) references movies(id)
);

create table ratings(
	movieId varchar(10) not null DEFAULT '',
    rating float not null,
    numVotes integer not null,
    foreign key(movieId) references movies(id)
);
