#pragma once
#include "Campaign_Types.h"
#include "JA2Types.h"
#include "Sound_Control.h"
#include <vector>

class JsonObjectReader;
class ItemSystem;

class VehicleModel
{
public:
	VehicleModel(SoundID enterSound, SoundID movementSound, ProfileID profileID, VehicleMovementType movementType, uint16_t armourType, uint8_t seats);
	static const VehicleModel* deserialize(JsonObjectReader& obj, const ItemSystem* itemSystem);
	static void validateData(const std::vector<const VehicleModel*>& vehicles);

	const SoundID   enter_sound;
	const SoundID   move_sound;
	const ProfileID profile;
	const VehicleMovementType   movement_type;
	const uint16_t  armour_type;
	const uint8_t   seats;
};
