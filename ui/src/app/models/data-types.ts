export class ResultsPerDate {
    'province_state': string;
    'source': string;
    'deaths': number;
    'confirmed': number;
    'recovered': number;
    'country': string;
    'long': number;
    'date': string;
    'lat': number;
    'active': number;
}

export class ResultMessage {
    'snapshots': ResultsPerDate[];
}
