/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.validation.rule;

/**
 * Validate rule used by {@link nl.tudelft.graphalytics.validation.VertexValidator} to compare
 * the value of a vertex given by a platform to the value of the vertex in the reference output.
 * The simplest rule is to check if the values are identical (see {@link nl.tudelft.graphalytics.validation.MatchLongValidationRule}),
 * however sometimes other comparisons are necessary, such as check if two doubles are
 * within a certain threshold of each other.
 *
 * @param <E> Type of the vertex value.
 */
public interface ValidationRule<E> {
	public E parse(String val) throws Throwable;
	public boolean match(E lhs, E rhs);
}
