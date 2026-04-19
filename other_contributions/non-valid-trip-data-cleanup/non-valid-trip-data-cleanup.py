import pandas as pd
import numpy as np

def calculate_distance(lat1, lon1, lat2, lon2):
    R = 6371000
    phi1, phi2 = np.radians(lat1), np.radians(lat2)
    dphi = np.radians(lat2 - lat1)
    dlambda = np.radians(lon2 - lon1)
    a = np.sin(dphi/2)**2 + np.cos(phi1)*np.cos(phi2)*np.sin(dlambda/2)**2
    return 2 * R * np.arctan2(np.sqrt(a), np.sqrt(1 - a))

def load_arff(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    data_start = 0
    columns = []
    arff_header = []
    
    for i, line in enumerate(lines):
        arff_header.append(line)
        if line.upper().startswith('@ATTRIBUTE'):
            parts = line.split()
            columns.append(parts[1].replace("'", ""))
        elif line.strip().upper() == '@DATA':
            data_start = i + 1
            break
            
    data_lines = [line.strip().split(',') for line in lines[data_start:] if line.strip()]
    df = pd.DataFrame(data_lines, columns=columns)
    
    df = df.replace('?', np.nan)
    
    for col in columns[2:]:
        df[col] = pd.to_numeric(df[col])
        
    return df, arff_header, columns

def trim_trip(df_trip):
    if len(df_trip) < 3:
        return df_trip
        
    transport_type = df_trip['transportType'].iloc[0]
    
    if transport_type == 'TRAIN':
        high_speeds = df_trip.index[df_trip['speed_kmh'] > 15]
        if len(high_speeds) > 0:
            last_peak = high_speeds[-1]
            cut_point = min(last_peak + 2, df_trip.index[-1])
            return df_trip.loc[:cut_point]
            
    elif transport_type == 'METRO':
        walk_candidates = df_trip.index[
            (df_trip['speed_kmh'] > 1.5) & (df_trip['speed_kmh'] < 8.0)
        ]
        
        if len(walk_candidates) > 0:
            trip_halfway = df_trip.index[len(df_trip) // 2]
            valid_walks = [idx for idx in walk_candidates if idx > trip_halfway]
            
            if valid_walks:
                cut_point = valid_walks[0]
                safe_cut = max(0, cut_point - 2)
                return df_trip.loc[:safe_cut]
                
    return df_trip

def export_to_arff(df, arff_header, original_columns, filename):
    df_export = df[original_columns]
    csv_data = df_export.to_csv(index=False, header=False, na_rep='?', lineterminator='\n')
    
    with open(filename, 'w', encoding='utf-8') as f:
        f.writelines(arff_header)
        f.write(csv_data)

def process_data():
    input_file = 'dataset_otpcd_viagens_nao_validas.arff'
    output_file = 'dataset_otpcd_viagens_limpas.arff'
    
    df, arff_header, original_columns = load_arff(input_file)
    
    df = df.sort_values(['userId', 'timestamp']).reset_index(drop=True)
    
    df['time_diff_s'] = df.groupby('userId')['timestamp'].diff() / 1000.0
    df['dist_meters'] = calculate_distance(
        df['latitude'].shift(1), df['longitude'].shift(1),
        df['latitude'], df['longitude']
    )
    df.loc[df['time_diff_s'].isna(), 'dist_meters'] = np.nan
    df['speed_kmh'] = (df['dist_meters'] / df['time_diff_s']) * 3.6
    
    df['is_new_trip'] = (df['time_diff_s'] > 600) | df['time_diff_s'].isna()
    df['trip_id'] = df['is_new_trip'].cumsum()
    
    cleaned_trips = []
    for trip_id in df['trip_id'].unique():
        trip = df[df['trip_id'] == trip_id].copy()
        cleaned_trip = trim_trip(trip)
        cleaned_trips.append(cleaned_trip)
        
    df_final = pd.concat(cleaned_trips)
    
    export_to_arff(df_final, arff_header, original_columns, output_file)
    print("Limpeza de dados concluída.")

if __name__ == "__main__":
    process_data()